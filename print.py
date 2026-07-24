import os

def merge_code_to_markdown(project_dir, output_file):
    # 需要扫描的文件后缀
    target_extensions = ('.kt', '.kts', '.toml')
    
    # 需要排除的目录（避免扫描依赖、缓存和隐藏文件）
    exclude_dirs = {'.git', '.gradle', '.idea', 'build', 'target', '.ipynb_checkpoints'}

    with open(output_file, 'w', encoding='utf-8') as md_file:
        md_file.write(f"# Project Code Export\n\n")
        md_file.write(f"Generated from: `{os.path.abspath(project_dir)}`\n\n---\n\n")

        for root, dirs, files in os.walk(project_dir):
            # 原地修改 dirs，排除不需要的目录，防止 os.walk 进入
            dirs[:] = [d for d in dirs if d not in exclude_dirs and not d.startswith('.')]

            for file in files:
                if file.endswith(target_extensions):
                    full_path = os.path.join(root, file)
                    # 计算相对路径，让文档结构更清晰
                    rel_path = os.path.relpath(full_path, project_dir)
                    
                    # 获取后缀用来做 Markdown 代码块的高亮
                    ext = os.path.splitext(file)[1].lstrip('.')
                    if ext == 'kts':
                        ext = 'kotlin'  # kts 使用 kotlin 高亮
                    elif ext == 'kt':
                        ext = 'kotlin'

                    print(f"正在处理: {rel_path}")

                    try:
                        with open(full_path, 'r', encoding='utf-8', errors='replace') as f:
                            content = f.read()
                        
                        # 写入 Markdown 格式
                        md_file.write(f"### `{rel_path}`\n\n")
                        md_file.write(f"```{ext}\n")
                        md_file.write(content)
                        md_file.write("\n```\n\n---\n\n")
                        
                    except Exception as e:
                        print(f"读取文件失败 {rel_path}: {e}")

    print(f"\n成功！所有内容已合并至: {os.path.abspath(output_file)}")

if __name__ == "__main__":
    # 配置你的项目路径和输出的 Markdown 文件名
    PROJECT_PATH = "."  # "." 代表当前目录，也可以改成绝对路径如 "/path/to/android-project"
    OUTPUT_MD = "project_context.md"
    
    merge_code_to_markdown(PROJECT_PATH, OUTPUT_MD)
