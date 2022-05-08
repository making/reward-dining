package lol.maki.rewarddining.account.web;

import java.util.List;

import am.ik.yavi.core.ConstraintViolation;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validated;
import lol.maki.rewarddining.account.Account;
import lol.maki.rewarddining.account.AccountManager;
import lol.maki.rewarddining.account.StubAccountManager;
import org.junit.jupiter.api.Test;

import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.support.SimpleSessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AccountControllerTest {
	private static final long VALID_ACCOUNT_ID = 0L;

	private static final long ILLEGAL_ACCOUNT_ID = 10101;

	AccountManager manager = new StubAccountManager();

	AccountController controller = new AccountController(manager);

	@Test
	void getAccountDetails() {
		ExtendedModelMap model = new ExtendedModelMap();
		controller.getAccountDetails(VALID_ACCOUNT_ID, model);
		AccountForm account = (AccountForm) model.get("account");
		assertNotNull(account);
		assertEquals(Long.valueOf(0), account.getId());
	}

	@SuppressWarnings("unchecked")
	@Test
	void getAccountList() {
		ExtendedModelMap model = new ExtendedModelMap();
		controller.getAccountList(model);
		List<Account> accounts = (List<Account>) model.get("accounts");
		assertNotNull(accounts);
		assertEquals(getNumAccountsExpected(), accounts.size());
		assertEquals(Long.valueOf(0), accounts.get(0).getId());
	}


//	@Test
//	void invalidId() {
//		assertThrows(ObjectRetrievalFailureException.class, () -> {
//			ExtendedModelMap model = new ExtendedModelMap();
//			controller.getAccountDetails(ILLEGAL_ACCOUNT_ID, model);
//		});
//	}

	@Test
	void validateAllValid() {
		final Validated<Account> validated = new AccountForm(0L, "123456789", "Ben").toAccount();
		assertThat(validated.isValid()).isTrue();
	}

	@Test
	void validateInvalidName() {
		final Validated<Account> validated = new AccountForm(0L, "123456789", "").toAccount();
		assertThat(validated.isValid()).isFalse();
		final ConstraintViolations violations = validated.errors();
		assertThat(violations).hasSize(1);
		final ConstraintViolation violation = violations.get(0);
		assertThat(violation.name()).isEqualTo("name");
		assertThat(violation.messageKey()).isEqualTo("charSequence.notBlank");
	}

	@Test
	void validateInvalidNumber() {
		final Validated<Account> validated = new AccountForm(0L, "", "Ben").toAccount();
		assertThat(validated.isValid()).isFalse();
		final ConstraintViolations violations = validated.errors();
		assertThat(violations).hasSize(2);
		{
			final ConstraintViolation violation = violations.get(0);
			assertThat(violation.name()).isEqualTo("number");
			assertThat(violation.messageKey()).isEqualTo("charSequence.notBlank");
		}
		{
			final ConstraintViolation violation = violations.get(1);
			assertThat(violation.name()).isEqualTo("number");
			assertThat(violation.messageKey()).isEqualTo("charSequence.pattern");
			assertThat(violation.message()).isEqualTo("\"number\" must be a 9 digit number");
		}
	}

	@Test
	void validateAllInvalid() {
		final Validated<Account> validated = new AccountForm(null, null, null).toAccount();
		assertThat(validated.isValid()).isFalse();
		final ConstraintViolations violations = validated.errors();
		assertThat(violations).hasSize(3);
		{
			final ConstraintViolation violation = violations.get(0);
			assertThat(violation.name()).isEqualTo("id");
			assertThat(violation.messageKey()).isEqualTo("object.notNull");
		}
		{
			final ConstraintViolation violation = violations.get(1);
			assertThat(violation.name()).isEqualTo("number");
			assertThat(violation.messageKey()).isEqualTo("charSequence.notBlank");
		}
		{
			final ConstraintViolation violation = violations.get(2);
			assertThat(violation.name()).isEqualTo("name");
			assertThat(violation.messageKey()).isEqualTo("charSequence.notBlank");
		}
	}

	@Test
	void editAccount() throws Exception {
		ExtendedModelMap model = new ExtendedModelMap();
		controller.getEditAccount(VALID_ACCOUNT_ID, model);
		AccountForm account = (AccountForm) model.get("account");
		assertNotNull(account);
		assertEquals(Long.valueOf(0), account.getId());
	}

	@Test
	void successfulPost() throws Exception {
		ExtendedModelMap model = new ExtendedModelMap();
		controller.getEditAccount(VALID_ACCOUNT_ID, model);
		AccountForm account = (AccountForm) model.get("account");
		account.setName("Ben");
		account.setNumber("987654321");
		BindingResult br = new MapBindingResult(model, "account");
		RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();
		String viewName = controller.postEditAccount(account, br, new SimpleSessionStatus(), attributes, model);
		Account modifiedAccount = manager.getAccount(VALID_ACCOUNT_ID);
		assertEquals("Ben", modifiedAccount.getName(), "Object name has not been updated by post");
		assertEquals("987654321", modifiedAccount.getNumber(), "Object number has not been updated by post");
		assertEquals("redirect:/accountDetails", viewName,
				"Post has not redirected to the correct URL");
		assertEquals(String.valueOf(VALID_ACCOUNT_ID), attributes.get("id"));
	}

	@Test
	void unsuccessfulPost() throws Exception {
		ExtendedModelMap model = new ExtendedModelMap();
		controller.getEditAccount(VALID_ACCOUNT_ID, model);
		AccountForm account = (AccountForm) model.get("account");
		account.setName("");
		account.setNumber("");
		BindingResult br = new MapBindingResult(model, "account");
		String viewName = controller.postEditAccount(account, br, new SimpleSessionStatus(), new RedirectAttributesModelMap(), model);
		assertEquals("editAccount", viewName, "Invalid Post has not returned to correct view");
	}

	private int getNumAccountsExpected() {
		return StubAccountManager.NUM_ACCOUNTS_IN_STUB;
	}
}