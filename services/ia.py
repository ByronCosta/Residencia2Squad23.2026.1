# ia.py
import json
from ultralytics import YOLO

model = YOLO("best.pt") 

def analisar_planta(caminho_imagem: str) -> dict:
    results = model(caminho_imagem)
    itens_detectados = []
    contador_estacao = 1
    
    for r in results:
        for box in r.boxes:
            class_id = int(box.cls)
            label = model.names[class_id].lower() # Retorna 'dev', 'design' ou 'simples'
            
            # Coordenadas do Box
            xyxy = box.xyxy[0].tolist()
            coord_x = int((xyxy[0] + xyxy[2]) / 2) # Centro X
            coord_y = int((xyxy[1] + xyxy[3]) / 2) # Centro Y
            
            estacao = {
                "estacao": contador_estacao,
                "descricao": label,
                "coordx": coord_x,
                "coordy": coord_y
            }
            itens_detectados.append(estacao)
            contador_estacao += 1

    return {
        "itens": itens_detectados,
        "total_itens": len(itens_detectados),
        "observacoes_gerais": f"A IA detectou um total de {len(itens_detectados)} estações."
    }