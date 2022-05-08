package lol.maki.rewarddining.account.web;

import java.util.List;

import am.ik.yavi.core.Validated;
import lol.maki.rewarddining.account.Account;
import lol.maki.rewarddining.account.Beneficiary;

public record AccountRequest(String number, String name,
							 List<Beneficiary> beneficiaries) {
	public Validated<Account> toAccount() {
		return Account.forCreate(number, name)
				.peek(account -> beneficiaries.forEach(account::restoreBeneficiary));
	}
}
