package lol.maki.rewarddining.account.web;

import java.util.stream.Collectors;

import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validated;
import lol.maki.rewarddining.account.Account;
import lol.maki.rewarddining.account.AccountManager;
import lol.maki.rewarddining.account.Beneficiary;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * A Spring MVC @Controller controller handling requests to view and modify
 * Account information.
 * <p>
 * Note that all the Account application classes are imported from the
 * <tt>rewards-db</tt> project:
 * <ul>
 * <li>Domain objects: {@link Account} and {@link Beneficiary}</li>
 * <li>Service layer: {@link AccountManager} interface and its implementations</li>
 * <li>No repository layer is being used - the account-manager does everything</li>
 */
@Controller
@SessionAttributes("account")
public class AccountController {

	private final AccountManager accountManager;

	/**
	 * Creates a new AccountController with a given account manager.
	 */
	public AccountController(AccountManager accountManager) {
		this.accountManager = accountManager;
	}

	/**
	 * <p>Provide a model with an account for the account detail page.</p>
	 *
	 * @param id the id of the account
	 * @param model the "implicit" model created by Spring MVC
	 */
	@GetMapping("/accountDetails")
	public String getAccountDetails(@RequestParam("id") long id, Model model) {
		model.addAttribute("account", AccountForm.fromAccount(this.accountManager.getAccount(id)));
		return "accountDetails";
	}

	/**
	 * <p>Provide a model with a list of all accounts for the account List page.</p>
	 *
	 * @param model the "implicit" model created by Spring MVC
	 */
	@GetMapping("/accountList")
	public String getAccountList(Model model) {
		model.addAttribute("accounts", this.accountManager.getAllAccounts());
		return "accountList";
	}

	@GetMapping("/editAccount")
	public String getEditAccount(@RequestParam("id") long id, Model model) {
		model.addAttribute("account", AccountForm.fromAccount(this.accountManager.getAccount(id)));
		return "editAccount";
	}

	@PostMapping("/editAccount")
	public String postEditAccount(@ModelAttribute("account") AccountForm form, BindingResult bindingResult, SessionStatus status, RedirectAttributes attributes, Model model) {
		final Validated<Account> validated = form.toAccount();
		return validated.fold(errors -> {
			ConstraintViolations.of(errors).apply(bindingResult::rejectValue);
			model.addAttribute("fieldErrors", bindingResult.getFieldErrors().stream().collect(Collectors.groupingBy(FieldError::getField)));
			return "editAccount";
		}, account -> {
			this.accountManager.update(account);
			status.setComplete();
			attributes.addAttribute("id", account.getId());
			return "redirect:/accountDetails";
		});
	}
}
