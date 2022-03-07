/**
 * bootstrapのdatepickerのオプション設定.
 *
 * @author 松尾
 */
$('.datepicker').datepicker({
	autoclose: true,		// 日付選択時に自動でピッカーを閉じる
	assumeNearbyYear: true,	// 年が2桁の場合に自動で4桁に変更(19/1/2→2019/01/02)
	clearBtn: true,			// 入力クリアボタン表示
	maxViewMode: 3,			// 表示モードの上限(3:10年)
	todayBtn: true,			// 今日ボタン
	todayHighlight: true,	// 現在日付をハイライト
	format: 'yyyy/mm/dd',	// 日付フォーマット
	language: 'ja',			// 言語設定
	startDate: '1900-01-01',// 選択可能な日付の下限
	endDate: '0d'			// 選択可能な日付の上限（0d：今日）
});