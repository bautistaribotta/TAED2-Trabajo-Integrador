# Arma la carpeta y el ZIP de entrega a partir del último commit, sin archivos de trabajo.
# Uso: python armar_entrega.py <repo> <carpeta_salida> <pdf_del_informe>
import io
import os
import shutil
import subprocess
import sys
import zipfile

REPO, SALIDA, PDF = sys.argv[1], sys.argv[2], sys.argv[3]
NOMBRE = 'TPI-TAED2-Contreras-Ribotta'
destino = os.path.join(SALIDA, NOMBRE)

# Lo que no va en la entrega: instrucciones para agentes, planes, pendientes y el Word.
EXCLUIDOS = {'AGENTS.md', '.gitignore', 'docs/plan-caso-1.md', 'docs/plan-caso-2.md',
             'docs/pendientes.md', 'docs/Trabajo Practico Integrador.docx',
             'docs/armar_entrega.py'}

if os.path.exists(destino):
    shutil.rmtree(destino)
os.makedirs(destino)

# git archive toma solo los archivos versionados del último commit (sin target/ ni .git).
archivo = subprocess.run(['git', '-C', REPO, 'archive', '--format=zip', 'HEAD'],
                         check=True, capture_output=True).stdout
with zipfile.ZipFile(io.BytesIO(archivo)) as z:
    for nombre in z.namelist():
        if nombre.endswith('/') or nombre in EXCLUIDOS:
            continue
        ruta = os.path.join(destino, *nombre.split('/'))
        os.makedirs(os.path.dirname(ruta), exist_ok=True)
        with open(ruta, 'wb') as f:
            f.write(z.read(nombre))

# README: solo la documentación del proyecto, sin la copia del enunciado ni los planes.
readme = os.path.join(destino, 'README.md')
texto = open(readme, encoding='utf-8').read()
corte = texto.find('\n---\n\n# Trabajo Práctico Integrador')
assert corte > 0
texto = texto[:corte].rstrip() + '\n'
viejo = texto[texto.find('Este README tiene dos partes'):texto.find('## Requisitos')]
assert 'plan-caso-1.md' in viejo
texto = texto.replace(viejo, 'El informe del trabajo, con el análisis, las decisiones de diseño, los diagramas, '
                             'el pseudocódigo y los resultados de las pruebas, está en '
                             '`Trabajo Practico Integrador.pdf`.\n\n')
texto = texto.replace('coinciden con los calculados a mano en el plan.', 'coinciden con los calculados a mano antes de programar.')
for palabra in ('plan-caso', 'pendientes.md', 'AGENTS'):
    assert palabra not in texto, palabra
open(readme, 'w', encoding='utf-8', newline='\n').write(texto)

shutil.copy(PDF, os.path.join(destino, 'Trabajo Practico Integrador.pdf'))
if os.path.isdir(os.path.join(destino, 'docs')) and not os.listdir(os.path.join(destino, 'docs')):
    os.rmdir(os.path.join(destino, 'docs'))

# ZIP con una carpeta raíz.
zip_ruta = os.path.join(SALIDA, NOMBRE + '.zip')
if os.path.exists(zip_ruta):
    os.remove(zip_ruta)
with zipfile.ZipFile(zip_ruta, 'w', zipfile.ZIP_DEFLATED) as z:
    for raiz, _, archivos in os.walk(destino):
        for a in sorted(archivos):
            completo = os.path.join(raiz, a)
            z.write(completo, os.path.relpath(completo, SALIDA))
with zipfile.ZipFile(zip_ruta) as z:
    for n in z.namelist():
        print(n)
print(zip_ruta, os.path.getsize(zip_ruta), 'bytes')
