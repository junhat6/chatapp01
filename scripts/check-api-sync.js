#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

/**
 * APIパスの同期チェックスクリプト
 * バックエンドのApiPaths.ktとフロントエンドのapiPaths.tsの整合性を確認
 */

const BACKEND_FILE = 'backend/src/main/kotlin/com/usjchatapp/common/constants/ApiPaths.kt';
const FRONTEND_FILE = 'frontend/src/constants/apiPaths.ts';

// Kotlinファイルからパスを抽出
function extractKotlinPaths(content) {
    const paths = {};
    
    // object定義を抽出
    const objectRegex = /object\s+(\w+)\s*\{([^}]+)\}/g;
    let match;
    
    while ((match = objectRegex.exec(content)) !== null) {
        const objectName = match[1];
        const objectContent = match[2];
        
        // const val定義を抽出
        const constRegex = /const\s+val\s+(\w+)\s*=\s*"([^"]+)"/g;
        let constMatch;
        
        while ((constMatch = constRegex.exec(objectContent)) !== null) {
            const key = `${objectName}.${constMatch[1]}`;
            const value = constMatch[2];
            paths[key] = value;
        }
    }
    
    return paths;
}

// TypeScriptファイルからパスを抽出
function extractTypeScriptPaths(content) {
    const paths = {};
    
    // export const定義を抽出（ネストしたオブジェクトも考慮）
    const exportRegex = /export\s+const\s+(\w+)\s*=\s*\{([\s\S]*?)\}\s*as\s+const;/g;
    let match;
    
    while ((match = exportRegex.exec(content)) !== null) {
        const objectName = match[1];
        const objectContent = match[2];
        
        // 文字列プロパティを抽出
        const stringPropRegex = /(\w+):\s*[`'"](.*?)[`'"],?/g;
        let propMatch;
        
        while ((propMatch = stringPropRegex.exec(objectContent)) !== null) {
            const key = `${objectName}.${propMatch[1]}`;
            const value = propMatch[2];
            paths[key] = value;
        }
        
        // template literal プロパティを抽出
        const templateRegex = /(\w+):\s*`([^`]*)`/g;
        let templateMatch;
        
        while ((templateMatch = templateRegex.exec(objectContent)) !== null) {
            const key = `${objectName}.${templateMatch[1]}`;
            const value = templateMatch[2];
            paths[key] = value;
        }
    }
    
    return paths;
}

// パスの比較
function comparePaths(kotlinPaths, tsPaths) {
    const issues = [];
    
    // マッピングテーブル（Kotlin名 → TypeScript名）
    const nameMapping = {
        'Auth': 'AUTH_PATHS',
        'Profile': 'PROFILE_PATHS',
        'MatchingRequest': 'MATCHING_REQUEST_PATHS',
        'MatchingApplication': 'MATCHING_APPLICATION_PATHS',
        'MatchingRoom': 'MATCHING_ROOM_PATHS',
        'ChatRoom': 'CHAT_ROOM_PATHS',
        'Health': 'HEALTH_PATHS',
        'WebSocket': 'WEBSOCKET_PATHS'
    };
    
    // パス値の正規化（文字列補間の違いを吸収）
    function normalizePath(path) {
        return path
            .replace(/\$API_BASE/g, '${API_BASE}')  // Kotlin → TypeScript
            .replace(/\$\{API_BASE\}/g, '/api');   // TypeScript → 実際の値
    }
    
    // バックエンドのBASE + 相対パスを完全パスに変換
    function resolveKotlinPath(kotlinObject, kotlinProp, kotlinValue, kotlinPaths) {
        const baseKey = `${kotlinObject}.BASE`;
        const basePath = kotlinPaths[baseKey];
        
        if (basePath && kotlinValue.startsWith('/') && !kotlinValue.startsWith('/api')) {
            // 相対パスの場合、BASEパスと結合
            return normalizePath(basePath + kotlinValue);
        }
        return normalizePath(kotlinValue);
    }
    
    // Kotlinのパスに対してTypeScriptの対応をチェック
    for (const [kotlinKey, kotlinValue] of Object.entries(kotlinPaths)) {
        const [kotlinObject, kotlinProp] = kotlinKey.split('.');
        const tsObject = nameMapping[kotlinObject];
        
        if (tsObject) {
            const tsKey = `${tsObject}.${kotlinProp}`;
            const tsValue = tsPaths[tsKey];
            
            if (!tsValue) {
                issues.push(`❌ TypeScript側に対応するパスがありません: ${tsKey}`);
            } else {
                const resolvedKotlinPath = resolveKotlinPath(kotlinObject, kotlinProp, kotlinValue, kotlinPaths);
                const normalizedTs = normalizePath(tsValue);
                
                if (resolvedKotlinPath !== normalizedTs) {
                    issues.push(`⚠️  パスの値が異なります: ${kotlinKey}="${kotlinValue}" vs ${tsKey}="${tsValue}"`);
                }
            }
        }
    }
    
    // TypeScript独自のパスをチェック（情報提供のみ）
    const infoMessages = [];
    for (const [tsKey, tsValue] of Object.entries(tsPaths)) {
        const [tsObject, tsProp] = tsKey.split('.');
        const kotlinObject = Object.keys(nameMapping).find(key => nameMapping[key] === tsObject);
        
        if (kotlinObject) {
            const kotlinKey = `${kotlinObject}.${tsProp}`;
            if (!kotlinPaths[kotlinKey]) {
                infoMessages.push(`ℹ️  TypeScript独自のパス: ${tsKey}="${tsValue}"`);
            }
        }
    }
    
    return { issues, infoMessages };
}

// メイン実行
function main() {
    console.log('🔍 APIパス同期チェックを開始します...\n');
    
    try {
        // ファイル読み込み
        const kotlinContent = fs.readFileSync(BACKEND_FILE, 'utf8');
        const tsContent = fs.readFileSync(FRONTEND_FILE, 'utf8');
        
        // パス抽出
        const kotlinPaths = extractKotlinPaths(kotlinContent);
        const tsPaths = extractTypeScriptPaths(tsContent);
        
        console.log(`📂 Kotlin定義: ${Object.keys(kotlinPaths).length}個のパス`);
        console.log(`📂 TypeScript定義: ${Object.keys(tsPaths).length}個のパス\n`);
        
        // 比較
        const { issues, infoMessages } = comparePaths(kotlinPaths, tsPaths);
        
        if (issues.length === 0) {
            console.log('✅ APIパスの同期に問題はありません！');
            
            if (infoMessages.length > 0) {
                console.log('\n📝 情報提供:');
                infoMessages.slice(0, 5).forEach(info => console.log(info));
                if (infoMessages.length > 5) {
                    console.log(`... と他 ${infoMessages.length - 5} 個の情報`);
                }
            }
            
            process.exit(0);
        } else {
            console.log('🚨 APIパスの同期に以下の問題があります:\n');
            issues.forEach(issue => console.log(issue));
            console.log(`\n合計 ${issues.length} 個の問題が見つかりました。`);
            
            if (infoMessages.length > 0) {
                console.log('\n📝 その他の情報:');
                infoMessages.slice(0, 3).forEach(info => console.log(info));
                if (infoMessages.length > 3) {
                    console.log(`... と他 ${infoMessages.length - 3} 個の情報`);
                }
            }
            
            process.exit(1);
        }
        
    } catch (error) {
        console.error('❌ エラーが発生しました:', error.message);
        process.exit(1);
    }
}

// 直接実行された場合のみ実行
if (require.main === module) {
    main();
}

module.exports = { main, extractKotlinPaths, extractTypeScriptPaths, comparePaths }; 