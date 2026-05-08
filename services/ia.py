import json
from ultralytics import YOLO

# Carrega o modelo (ex: yolov8n.pt)
model = YOLO("yolov8n.pt") 

def analisar_planta(caminho_imagem: str) -> dict:
    # Realiza a detecção de objetos
    results = model(caminho_imagem)
    
    itens_detectados = []
    
    # Processa os resultados do YOLO
    for r in results:
        for box in r.boxes:
            class_id = int(box.cls)
            label = model.names[class_id]
            conf = float(box.conf)
            
            # Formata o item individual
            item = {
                "nome": label,
                "tipo": label,
                "quantidade": 1,
                "observacoes": f"Confiança: {conf:.2f}"
            }
            itens_detectados.append(item)

    # 1. Cria o resumo descritivo para as observações gerais
    total = len(itens_detectados)
    resumo_texto = f"A IA detectou um total de {total} objetos na imagem."

    # 2. Monta o dicionário final para enviar ao Java
    resultado_final = {
        "itens": itens_detectados,
        "total_itens": total,
        "observacoes_gerais": resumo_texto
    }

    return resultado_final
