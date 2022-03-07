package redmineissuesoutput.domain.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.User;

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
	private int sequence;		//	番号
	private int id;					//	id
	private String tracker;			//	区分				トラッカー			Issue.getTracker().getName()
	private String number;			//	管理番号			管理番号			CustomField.getCustomFieldByName("管理番号").getValue()
	private String dept;			//	部門				部門				CustomField.getCustomFieldByName("部門").getValue()
	private String accrualDate;		//	発生日			発生日			CustomField.getCustomFieldByName("発生日").getValue()
	private String originator;		//	発信元担当者		発信元担当者		CustomField.getCustomFieldByName("発信元担当者").getValue()
	private String status;			//	状況				ステータス			Issue.getStatusName()
	private String agenda;			//	検討課題			説明				Issue.getDescription()
	private String assigneeName;	//	検討者（主）		担当者			Issue.getAssigneeName()
	private String subAssigneeName;	//	検討者（副）		副担当者			CustomField.getCustomFieldByName("副担当者").getValue()
	private String result;			//	検討案または結果	検討案または結果	CustomField.getCustomFieldByName("検討案または結果").getValue()
	private String dueDate;			//	期限				期日				Issue.getDueDate()
	private String completionDate;	//	回答または完了日	完了日			CustomField.getCustomFieldByName("完了日").getValue()
	
	public Ticket(Issue issue, int sec, List<User> userList) {
		this.sequence = sec;
		this.id = issue.getId();
		this.tracker = issue.getTracker().getName();
		this.number = issue.getCustomFieldByName("管理番号").getValue();
		this.dept = issue.getCustomFieldByName("部門").getValue();
		this.accrualDate = LocalDate
				.parse(issue.getCustomFieldByName("発生日").getValue(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
				.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		this.originator = issue.getCustomFieldByName("発信元担当者").getValue();
		this.status = issue.getStatusName();
		this.agenda = issue.getDescription();
		if (Objects.nonNull(issue.getAssigneeName())) {
			int endIdx = issue.getAssigneeName().indexOf(' ');
			this.assigneeName = issue.getAssigneeName().substring(0, endIdx + 1);
		}
		if (issue.getCustomFieldByName("副担当者").getValues().size() > 0) {
			StringBuilder sb = new StringBuilder();
			int i = 0;
			for (String id : issue.getCustomFieldByName("副担当者").getValues()) {
				if (i != 0) {
					sb.append("\n");
				}
				Optional<User> op = userList.stream().filter(u -> Objects.equals(u.getId(), Integer.valueOf(id))).findFirst();
				if (op.isPresent()) {
					sb.append(op.get().getLastName());
					i++;
				}
			}
			this.subAssigneeName = sb.toString();
		}
		this.result = issue.getCustomFieldByName("検討案または結果").getValue();
		if (Objects.nonNull(issue.getDueDate())) {
			this.dueDate = new SimpleDateFormat("yyyy/MM/dd").format(issue.getDueDate());
		}
		if (!issue.getCustomFieldByName("完了日").getValue().isEmpty()) {
			this.completionDate = LocalDate
					.parse(issue.getCustomFieldByName("完了日").getValue(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
					.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		}
	}
}
