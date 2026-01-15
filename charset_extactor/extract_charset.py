import os
from pathlib import Path

def extract_unique_chars():
    lang_dir = Path(__file__).parent / "lang"
    unique_chars = set()

    for lang_file in lang_dir.rglob("*.lang"):
        with open(lang_file, "r", encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if not line or line.startswith("#"):
                    continue
                if "=" in line:
                    value = line.split("=", 1)[1]
                    unique_chars.update(value)

    unique_chars.discard('\n')
    unique_chars.discard('\r')

    sorted_chars = sorted(unique_chars)
    result = "".join(sorted_chars)

    output_path = Path(__file__).parent / "output.txt"
    with open(output_path, "w", encoding="utf-8") as f:
        f.write(result)

    print(f"총 {len(unique_chars)}개의 고유 문자를 추출했습니다.")
    print(f"결과가 {output_path}에 저장되었습니다.")

if __name__ == "__main__":
    extract_unique_chars()
