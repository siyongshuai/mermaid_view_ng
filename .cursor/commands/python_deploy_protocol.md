# Python CLI 工具标准化部署协议 (Standard Deployment Protocol)

## 1. 概述 (Overview)
本协议旨在提供一套生产级的 Python CLI 工具安装与卸载方案，确保工具在用户系统中的**隔离性**、**稳健性**与**可维护性**。

## 2. 核心架构设计理念
- **隔离思维 (Isolation)**：源码与依赖完全托管在独立的虚拟环境中，不污染系统 Python。
- **运行态解耦 (Decoupling)**：采用“非编辑模式”安装，安装后的工具与开发目录物理隔离。
- **标识隔离与风格规范 (Naming & Style)**：
    - **包名 (`${PKG_NAME}`)**: 采用 `underline` (下划线/snake_case) 风格。用于标识内部虚拟环境路径、源码归属及 Python 模块命名。
    - **命令名 (`${CMD_NAME}`)**: 采用 `kebab-case` (连字符) 风格。用于标识用户交互指令、包装脚本文件名及 CLI 语义。
    - **完全隔离设计**: 确保安装位置与用户触发指令在命名空间上完全解耦，从视觉风格上即可区分“实现”与“接口”。
- **规范化布局**：
    - **本体 (Core)**: `~/.local/share/${PKG_NAME}/venv` (以包名命名的环境与源码)
    - **符号 (Entry)**: `~/.local/bin/${CMD_NAME}` (以命令名命名的轻量级包装脚本)
    - **状态 (State)**: `~/.${CMD_NAME}` (以命令名命名的用户配置与持久化数据)

---

## 3. 安装流程规范 (Install Specification)

### 3.1 环境预检
- 确保系统已安装 `python3` 和 `python3-venv`。
- 确认用户对 `~/.local/bin` 和 `~/.local/share` 具有写权限。

### 3.2 步骤详述
1. **创建沙箱**: 在 `~/.local/share/${PKG_NAME}/` 下建立专用的虚拟环境。
2. **物理同步 (Install)**: 
   - 激活 venv，升级 `pip`。
   - 执行 `pip install .`。此操作会将当前代码打包并复制到 venv 的 `site-packages` 中，实现与开发路径的完全解耦。
3. **入口注入**:
   - 在 `~/.local/bin/` 创建与 `${CMD_NAME}` 同名的 Bash 脚本。
   - 使用 `exec` 语法将所有参数 (`$@`) 转发至 `${PKG_NAME}` venv 内的二进制文件：
     ```bash
     #!/bin/bash
     VENV_PATH="$HOME/.local/share/${PKG_NAME}/venv"
     exec "${VENV_PATH}/bin/${CMD_NAME}" "$@"
     ```
4. **环境反馈**: 检测 `$PATH` 是否包含 `~/.local/bin`，若无则输出标准化的 `export` 引导。

---

## 4. 卸载流程规范 (Uninstall Specification)

### 4.1 核心原则
- **彻底性**: 默认删除所有运行代码及入口。
- **数据尊重**: 对用户配置目录进行差分处理，除非显式指定 `--all`，否则默认保留。

### 4.2 步骤详述
1. **移除运行态**: 递归删除 `~/.local/share/${PKG_NAME}/` 目录。
2. **清除符号**: 删除 `~/.local/bin/${CMD_NAME}` 入口文件。
3. **状态清理 (可选)**: 
   - 若检测到 `--all` 参数，进入交互式确认。
   - 告知用户即将删除的内容（如：数据库、配置文件、缓存）。
   - 确认后执行 `rm -rf ~/.${CMD_NAME}`。

---

## 5. 依赖清单 (Prerequisites)
- **操作系统**: 兼容 POSIX 的系统 (Linux, macOS, WSL2)。
- **工具链**: `bash`, `python3`, `python3-venv`, `pip`。

---

## 6. 元认知最佳实践 (Metacognitive Insights)
- **幂等性**: 脚本应支持重复运行而不报错。
- **原子性**: 安装过程应尽量模块化，确保在失败时能够通过卸载逻辑回滚。
- **生命周期意识**: 开发者应区分 **“代码执行空间”** 和 **“数据存储空间”**。卸载逻辑的设计体现了开发者对用户数据主权的尊重。

