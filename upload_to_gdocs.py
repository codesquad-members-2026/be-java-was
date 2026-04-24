#!/usr/bin/env python3
import subprocess
import tempfile
import markdown
from pathlib import Path
from googleapiclient.discovery import build
from googleapiclient.http import MediaFileUpload
from google.oauth2.credentials import Credentials

FOLDER_ID = "19MOKfjpS-2c7uo3aDtoDiV7NBvOQBYG_"

def get_token():
    result = subprocess.run(
        [str(Path.home() / "Downloads/google-cloud-sdk/bin/gcloud"), "auth", "print-access-token"],
        capture_output=True, text=True
    )
    return result.stdout.strip()

def md_to_html(md_path):
    text = Path(md_path).read_text(encoding="utf-8")
    body = markdown.markdown(text, extensions=["fenced_code", "tables", "nl2br"])
    return f"""<!DOCTYPE html><html><head><meta charset="utf-8">
<style>
  body {{ font-family: Arial, sans-serif; font-size: 11pt; }}
  h1 {{ font-size: 20pt; }} h2 {{ font-size: 16pt; }} h3 {{ font-size: 13pt; }}
  code {{ background: #f4f4f4; padding: 2px 4px; font-family: monospace; }}
  pre  {{ background: #f4f4f4; padding: 10px; white-space: pre-wrap; }}
  table {{ border-collapse: collapse; }} td, th {{ border: 1px solid #ccc; padding: 6px; }}
</style></head><body>{body}</body></html>"""

def upload_as_gdoc(service, md_path, existing_file_id=None):
    html = md_to_html(md_path)
    with tempfile.NamedTemporaryFile(suffix=".html", delete=False, mode="w", encoding="utf-8") as f:
        f.write(html)
        tmp_path = f.name

    media = MediaFileUpload(tmp_path, mimetype="text/html", resumable=True)
    name = Path(md_path).stem

    if existing_file_id:
        file = service.files().update(
            fileId=existing_file_id,
            body={"name": name},
            media_body=media,
            fields="id,name,webViewLink"
        ).execute()
    else:
        file = service.files().create(
            body={"name": name, "mimeType": "application/vnd.google-apps.document", "parents": [FOLDER_ID]},
            media_body=media,
            fields="id,name,webViewLink"
        ).execute()

    Path(tmp_path).unlink()
    return file

def main():
    service = build("drive", "v3", credentials=Credentials(token=get_token()))

    # 기존 파일 ID (덮어쓰기)
    existing = {
        "Hana":        "1FPadNIcTgX5D5oogD0onN6DZUqPdO-XMl56HGeyETig",
        "LeeWanJa":    "153_4BnCHiO0xAKCRYFNIJOhgE2lvFjcvbHIUtWSn8LU",
        "gabi":        "1WZUjoNo1ksgpmNmD0FrkbG1WHQgI_Jpq0rXsSuG1X1o",
        "jwpark97114": "1ACRqBtYknRXEoPz3TzHzOyXOsT1N4ZgJeueWAFaCPUI",
    }

    md_files = ["Hana.md", "LeeWanJa.md", "gabi.md", "jwpark97114.md"]
    base_dir = Path(__file__).parent

    for filename in md_files:
        path = base_dir / filename
        if not path.exists():
            print(f"[SKIP] {filename} not found")
            continue
        name = path.stem
        file_id = existing.get(name)
        print(f"Uploading {filename}...", end=" ", flush=True)
        result = upload_as_gdoc(service, str(path), file_id)
        print(f"done → {result['webViewLink']}")

if __name__ == "__main__":
    main()
