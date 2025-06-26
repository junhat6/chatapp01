package com.usjchatapp.domain.enums

enum class USJAttraction(val displayName: String) {
    // ハリー・ポッター エリア
    HARRY_POTTER_FORBIDDEN_JOURNEY("ハリー・ポッター・アンド・ザ・フォービドゥン・ジャーニー"),
    FLIGHT_OF_THE_HIPPOGRIFF("フライト・オブ・ザ・ヒッポグリフ"),
    
    // スーパー・ニンテンドー・ワールド
    MARIO_KART_KOOPA_TROOPA_CHALLENGE("マリオカート ~クッパの挑戦状~"),
    YOSHIS_ADVENTURE("ヨッシーアドベンチャー"),
    
    // ミニオン・パーク
    MINION_MAYHEM("ミニオン・ハチャメチャ・ライド"),
    MINION_PARK_PLAYLAND("ミニオン・パーク プレイランド"),
    
    // ジュラシック・パーク
    FLYING_DINOSAUR("ザ・フライング・ダイナソー"),
    JURASSIC_PARK_THE_RIDE("ジュラシック・パーク・ザ・ライド"),
    
    // ハリウッド・ドリーム
    HOLLYWOOD_DREAM_BACKDROP("ハリウッド・ドリーム・ザ・ライド ~バックドロップ~"),
    HOLLYWOOD_DREAM("ハリウッド・ドリーム・ザ・ライド"),
    
    // その他の人気アトラクション
    SPACE_FANTASY("スペース・ファンタジー・ザ・ライド"),
    AMAZING_SPIDER_MAN("アメージング・アドベンチャー・オブ・スパイダーマン・ザ・ライド 4K3D"),
    TRANSFORMERS("トランスフォーマー・ザ・ライド-3D"),
    TERMINATOR("ターミネーター 2:3-D"),
    BACK_TO_THE_FUTURE("バック・トゥ・ザ・フューチャー・ザ・ライド"),
    JAWS("ジョーズ"),
    
    // ファミリー向け
    ELMO_LITTLE_DRIVE("エルモのリトル・ドライブ"),
    ELMO_GO_GO_SKATE("エルモのゴーゴー・スケートボード"),
    SESAME_STREET_BIG_BIRD_BIG_TOP_CIRCUS("セサミストリート・ビッグバード・ビッグトップ・サーカス"),
    SNOOPY_GREAT_RACE("スヌーピーのグレートレース"),
    FLYING_SNOOPY("フライング・スヌーピー"),
    
    // ショー・パレード
    WATER_WORLD("ウォーターワールド"),
    SING_ON_TOUR("シング・オン・ツアー"),
    
    // シーズンイベント
    HALLOWEEN_HORROR_NIGHTS("ハロウィーン・ホラー・ナイト"),
    CHRISTMAS_SHOW("クリスマス・ショー"),
    COOL_JAPAN("クール・ジャパン");
    
    companion object {
        fun fromDisplayName(displayName: String): USJAttraction? {
            return values().find { it.displayName == displayName }
        }
        
        fun getAllDisplayNames(): List<String> {
            return values().map { it.displayName }
        }
    }
} 