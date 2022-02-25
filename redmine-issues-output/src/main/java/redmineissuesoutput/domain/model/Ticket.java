package redmineissuesoutput.domain.model;

import java.io.Serializable;
import java.util.Date;

import com.taskadapter.redmineapi.bean.Issue;

import lombok.Getter;
import lombok.Setter;

/**
 * チケット単位のデータを持つクラス。
 * 
 * @author 松尾
 */
@SuppressWarnings("serial")
@Getter
@Setter
public class Ticket implements Serializable {
	private String sequence;		//	番号
	private String tracker;			//	区分				トラッカー			Issue.getTracker()
	private String number;			//	管理番号			管理番号			CustomField.getCustomFieldByName("管理番号");
	private String dept;			//	部門				部門				CustomField.getCustomFieldByName("部門");
	private String accrualDate;		//	発生日			発生日			CustomField.getCustomFieldByName("発生日");
	private String originator;		//	発信元担当者		発信元担当者		CustomField.getCustomFieldByName("発信元担当者");
	private String status;			//	状況				ステータス			Issue.getStatusName()
	private String agenda;			//	検討課題			説明				Issue.getDescription()
	private String assigneeName;	//	検討者（主）		担当者			Issue.getAssigneeName()
	private String subAssigneeName;	//	検討者（副）		副担当者			CustomField.getCustomFieldByName("副担当者")
	private String result;			//	検討案または結果	検討案または結果	CustomField.getCustomFieldByName("検討案または結果");
	private Date dueDate;			//	期限				期日				Issue.getDueDate();
	private String completionDate;	//	回答または完了日	完了日			CustomField.getCustomFieldByName("完了日");
	
	public Ticket(Issue issue, int sec) {
		this.sequence = String.valueOf(sec);
		this.tracker = issue.getTracker().getName();
		this.number = issue.getCustomFieldByName("管理番号").getValue();
		this.dept = issue.getCustomFieldByName("部門").getValue();
		this.accrualDate = issue.getCustomFieldByName("発生日").getValue();
		this.originator = issue.getCustomFieldByName("発信元担当者").getValue();
		this.status = issue.getStatusName();
		this.agenda = issue.getDescription();
		this.assigneeName = issue.getAssigneeName();
		this.subAssigneeName = issue.getCustomFieldByName("副担当者").getValue();
		this.result = issue.getCustomFieldByName("検討案または結果").getValue();
		this.dueDate = issue.getDueDate();
		this.completionDate = issue.getCustomFieldByName("完了日").getValue();
	}
}
