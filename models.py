# model.py
from pydantic import BaseModel
from typing import List, Optional

class EstacaoDetectada(BaseModel):
    estacao: int
    descricao: str  # 'dev', 'design' ou 'simples'
    coordx: int
    coordy: int

class AnaliseResponse(BaseModel):
    itens: List[EstacaoDetectada]
    total_itens: int
    observacoes_gerais: Optional[str] = None