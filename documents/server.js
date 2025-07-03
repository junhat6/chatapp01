const express = require('express');
const fs = require('fs-extra');
const path = require('path');
const { marked } = require('marked');
const hljs = require('highlight.js');

const app = express();
const PORT = 8100;

// 静的ファイルの提供
app.use(express.static('public'));

// Markdownの設定
marked.setOptions({
  highlight: function(code, lang) {
    // mermaidコードブロックはシンタックスハイライトをスキップ
    if (lang === 'mermaid') {
      return code;
    }
    
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(code, { language: lang }).value;
    }
    return hljs.highlightAuto(code).value;
  },
  breaks: true,
  gfm: true
});

// Mermaid.jsのレンダリング関数
function renderMermaid(content) {
  // mermaidコードブロックを検索して置換
  return content.replace(/```mermaid\n([\s\S]*?)\n```/g, (match, diagram) => {
    const id = 'mermaid-' + Math.random().toString(36).substr(2, 9);
    return `<div class="mermaid" id="${id}">${diagram}</div>`;
  });
}

// HTMLテンプレート
const htmlTemplate = (title, content, sidebar) => `
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${title} - マッチングシステムドキュメント</title>
    <link rel="stylesheet" href="/styles.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/styles/github.min.css">
    <script src="https://cdn.jsdelivr.net/npm/mermaid@10.6.1/dist/mermaid.min.js"></script>
</head>
<body>
    <div class="container">
        <header class="header">
            <h1>マッチングシステム ドキュメント</h1>
        </header>
        
        <div class="main-content">
            <aside class="sidebar">
                ${sidebar}
            </aside>
            
            <main class="content">
                ${content}
            </main>
        </div>
    </div>
    
    <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/highlight.min.js"></script>
    <script>
        // mermaidコードブロックを除外してシンタックスハイライトを実行
        hljs.highlightAll({
            ignoreUnescapedHTML: true,
            cssSelector: 'pre code:not(.language-mermaid)'
        });
        
        // Mermaid.jsの初期化
        mermaid.initialize({
            startOnLoad: true,
            theme: 'default',
            flowchart: {
                useMaxWidth: true,
                htmlLabels: true
            },
            sequence: {
                useMaxWidth: true,
                diagramMarginX: 50,
                diagramMarginY: 10,
                actorMargin: 50,
                width: 150,
                height: 65,
                boxMargin: 10,
                boxTextMargin: 5,
                noteMargin: 10,
                messageMargin: 35,
                mirrorActors: true,
                bottomMarginAdj: 1,
                useMaxWidth: true,
                rightAngles: false,
                showSequenceNumbers: false
            }
        });
    </script>
</body>
</html>
`;

// サイドバー生成
function generateSidebar(currentFile = '') {
  const files = [
    { name: 'README.md', title: '概要' },
    { name: 'matching-system-implementation.md', title: '実装ドキュメント' },
    { name: 'matching-system-sequence-diagrams.md', title: 'シーケンス図' },
    { name: 'matching-api-specification.md', title: 'API仕様書' }
  ];

  let sidebar = '<nav class="sidebar-nav"><ul>';
  
  files.forEach(file => {
    const isActive = currentFile === file.name ? ' class="active"' : '';
    sidebar += `<li${isActive}><a href="/${file.name}">${file.title}</a></li>`;
  });
  
  sidebar += '</ul></nav>';
  return sidebar;
}

// ルートパス - READMEを表示
app.get('/', async (req, res) => {
  try {
    const readmePath = path.join(__dirname, 'README.md');
    const content = await fs.readFile(readmePath, 'utf-8');
    const htmlContent = marked(content);
    const mermaidContent = renderMermaid(htmlContent);
    const sidebar = generateSidebar('README.md');
    
    res.send(htmlTemplate('概要', mermaidContent, sidebar));
  } catch (error) {
    res.status(500).send('ドキュメントの読み込みに失敗しました');
  }
});

// Markdownファイルの表示
app.get('/:filename', async (req, res) => {
  try {
    const filename = req.params.filename;
    const filePath = path.join(__dirname, filename);
    
    // ファイルの存在確認
    if (!await fs.pathExists(filePath)) {
      return res.status(404).send('ファイルが見つかりません');
    }
    
    const content = await fs.readFile(filePath, 'utf-8');
    const htmlContent = marked(content);
    const mermaidContent = renderMermaid(htmlContent);
    const sidebar = generateSidebar(filename);
    
    // タイトルを抽出
    const titleMatch = content.match(/^#\s+(.+)$/m);
    const title = titleMatch ? titleMatch[1] : filename;
    
    res.send(htmlTemplate(title, mermaidContent, sidebar));
  } catch (error) {
    res.status(500).send('ドキュメントの読み込みに失敗しました');
  }
});

app.listen(PORT, () => {
  console.log(`ドキュメントビューアーが起動しました: http://localhost:${PORT}`);
}); 