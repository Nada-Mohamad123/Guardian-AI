"""
GuardianAI Chatbot — FastAPI service
RAG + Gemini API

Endpoints:
  POST /chat        — ask a question, get an answer
  GET  /health      — liveness check
"""

from __future__ import annotations

import os
import re
import numpy as np
import google.generativeai as genai
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional

# ─── Config ───────────────────────────────────────────────
GEMINI_API_KEY = os.environ.get("GEMINI_API_KEY", "YOUR_API_KEY_HERE")
EMBED_MODEL    = "models/gemini-embedding-001"
LLM_MODEL      = "gemini-2.5-flash"
KB_PATH        = "GuardianAI_Features.md"   # place this file next to main.py

genai.configure(api_key=GEMINI_API_KEY)

# ─── Load & chunk knowledge base ──────────────────────────
def _load_kb() -> str:
    if os.path.exists(KB_PATH):
        with open(KB_PATH, "r", encoding="utf-8") as f:
            return f.read()
    raise FileNotFoundError(
        f"Knowledge-base file '{KB_PATH}' not found. "
        "Place GuardianAI_Features.md next to this file."
    )

def _chunk_markdown(text: str, max_chars: int = 900) -> list[str]:
    sections = re.split(r"\n(?=##? )", text)
    chunks: list[str] = []
    for sec in sections:
        sec = sec.strip()
        if not sec:
            continue
        if len(sec) <= max_chars:
            chunks.append(sec)
        else:
            paras, buf = sec.split("\n\n"), ""
            for p in paras:
                if len(buf) + len(p) < max_chars:
                    buf += "\n\n" + p
                else:
                    if buf.strip():
                        chunks.append(buf.strip())
                    buf = p
            if buf.strip():
                chunks.append(buf.strip())
    return [c for c in chunks if c]

# ─── Embed helpers ─────────────────────────────────────────
def _embed(text: str, task_type: str = "retrieval_document") -> np.ndarray:
    text = (text or "").strip()
    if not text:
        raise ValueError("Cannot embed empty text")
    result = genai.embed_content(
        model=EMBED_MODEL,
        content=text,
        task_type=task_type,
    )
    return np.array(result["embedding"], dtype=np.float32)

def _cosine(a: np.ndarray, b: np.ndarray) -> float:
    return float(np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b) + 1e-8))

def _top_k(query: str, k: int = 3) -> list[str]:
    q_emb = _embed(query, task_type="retrieval_query")
    sims  = np.array([_cosine(q_emb, c) for c in CHUNK_EMBEDDINGS])
    idxs  = sims.argsort()[::-1][:k]
    return [CHUNKS[i] for i in idxs]

# ─── Build index at startup ────────────────────────────────
print("Loading knowledge base…")
KB_TEXT         = _load_kb()
CHUNKS          = _chunk_markdown(KB_TEXT)
print(f"  {len(CHUNKS)} chunks loaded. Embedding…")
CHUNK_EMBEDDINGS = np.array([_embed(c) for c in CHUNKS])
print("  Index ready.")

# ─── LLM ──────────────────────────────────────────────────
llm = genai.GenerativeModel(LLM_MODEL)

SYSTEM_PROMPT = """\
You are GuardianAI Assistant, a friendly in-app helper for parents
using the GuardianAI parental-control app.
Answer ONLY using the CONTEXT provided below.
If the answer is not in the context, say you don't have that information
and suggest the parent check Settings or the Help section.
Keep answers short, warm, and easy for a non-technical parent to understand.
Reply in the same language the parent used (Arabic or English).\
"""

def _ask(question: str, k: int = 3) -> str:
    context = "\n\n---\n\n".join(_top_k(question, k=k))
    prompt  = (
        f"{SYSTEM_PROMPT}\n\n"
        f"CONTEXT:\n{context}\n\n"
        f"PARENT QUESTION:\n{question}\n\n"
        f"ANSWER:"
    )
    return llm.generate_content(prompt).text

# ─── FastAPI app ───────────────────────────────────────────
app = FastAPI(title="GuardianAI Chatbot", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# ─── Schemas ───────────────────────────────────────────────
class ChatRequest(BaseModel):
    question: str
    top_k: Optional[int] = 3

class ChatResponse(BaseModel):
    answer: str

# ─── Endpoints ────────────────────────────────────────────
@app.get("/health")
def health():
    return {"status": "ok", "chunks": len(CHUNKS)}


@app.post("/chat", response_model=ChatResponse)
def chat(body: ChatRequest):
    q = (body.question or "").strip()
    if not q:
        raise HTTPException(status_code=400, detail="question must not be empty")
    try:
        answer = _ask(q, k=body.top_k)
        return ChatResponse(answer=answer)
    except Exception as exc:
        raise HTTPException(status_code=500, detail=str(exc))