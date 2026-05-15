#!/usr/bin/env python3
import json
from datetime import datetime

def convert_localdatetime(obj):
    """Convierte el objeto LocalDateTime a string"""
    if isinstance(obj, dict) and 'date' in obj and 'time' in obj:
        # Formato: 2026-05-13 23:10:21
        date = obj['date']
        time = obj['time']
        return f"{date['year']:04d}-{date['month']:02d}-{date['day']:02d} {time['hour']:02d}:{time['minute']:02d}:{time['second']:02d}"
    return obj

def convert_file(input_file, output_file):
    with open(input_file, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    converted = []
    for item in data:
        new_item = {}
        for key, value in item.items():
            if key in ['horaIngreso', 'fechaAtencion']:
                new_item[key] = convert_localdatetime(value)
            else:
                new_item[key] = value
        converted.append(new_item)
    
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(converted, f, indent=2, ensure_ascii=False)
    
    print(f"✓ Convertidos {len(converted)} clientes")
    print(f"✓ Guardado en: {output_file}")

if __name__ == "__main__":
    convert_file('clientes.json', 'clientes_convertido.json')
    print("\n📌 Después de verificar, renombra:")
    print("   mv clientes_convertido.json clientes.json")