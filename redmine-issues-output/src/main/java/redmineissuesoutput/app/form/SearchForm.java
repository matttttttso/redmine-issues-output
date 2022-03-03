package redmineissuesoutput.app.form;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.Setter;

/**
 * 検索画面のFormの値を持つFormクラス。
 * 
 * @author 松尾
 */
@SuppressWarnings("serial")
@Getter
@Setter
public class SearchForm implements Serializable {
	@NotNull(message = "開始日：必須項目です")
	@PastOrPresent(message = "開始日：今日以前の日付を入力してください")
	@DateTimeFormat(pattern = "yyyy/MM/dd")
	private LocalDate startDate;				// 開始日
	
	@NotNull(message = "終了日：必須項目です")
	@PastOrPresent(message = "終了日：今日以前の日付を入力してください")
	@DateTimeFormat(pattern = "yyyy/MM/dd")
	private LocalDate endDate;					// 終了日
	
	@AssertTrue(message = "終了日は開始日以降を入力してください")
	public boolean isDateValid() {
		if (Objects.isNull(startDate) || Objects.isNull(endDate)) {
			return true;	// Nullチェックは単項目チェックで行う
		}
		return endDate.isAfter(startDate);
	}
	
//	@NotNull(message = "プロジェクトID：必須項目です")
//	public String projectId;					// プロジェクトID
//	
//	@NotNull(message = "顧客名：必須項目です")
//	public String customerName;					// 顧客名
}
