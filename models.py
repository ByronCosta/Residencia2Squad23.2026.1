from pydantic import BaseModel
from typing import List, Optional

class Item(BaseModel):
	nome : str
	tipo : str
	quantidade : int
	observações: Optional[str] = None
	
class Sala_Empresarial(BaseModel):
	item : List[Item]
	total_itens : int
	observacoes_gerais : Optional[str] = None
