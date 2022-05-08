package lol.maki.rewarddining.account.web;

import am.ik.yavi.core.Validated;
import lol.maki.rewarddining.account.Account;

public class AccountForm {
	private Long id;

	private String number;

	private String name;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AccountForm() {
	}

	public AccountForm(Long id, String number, String name) {
		this.id = id;
		this.number = number;
		this.name = name;
	}

	public static AccountForm fromAccount(Account account) {
		return new AccountForm(account.getId(), account.getNumber(), account.getName());
	}

	public Validated<Account> toAccount() {
		return Account.forUpdate(getId(), getNumber(), getName());
	}

	@Override
	public String toString() {
		return "AccountForm{" +
				"id=" + id +
				", number='" + number + '\'' +
				", name='" + name + '\'' +
				'}';
	}
}
