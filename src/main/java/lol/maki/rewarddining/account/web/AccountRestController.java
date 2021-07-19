package lol.maki.rewarddining.account.web;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lol.maki.rewarddining.account.Account;
import lol.maki.rewarddining.account.AccountManager;
import lol.maki.rewarddining.account.Beneficiary;
import lol.maki.rewarddining.account.Percentage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * A controller handling requests for CRUD operations on Accounts and their
 * Beneficiaries.
 */
@RestController
public class AccountRestController {

	private final Logger log = LoggerFactory.getLogger(AccountRestController.class);

	private final AccountManager accountManager;

	/**
	 * Creates a new AccountController with a given account manager.
	 */
	public AccountRestController(AccountManager accountManager) {
		this.accountManager = accountManager;
	}

	/**
	 * Provide a list of all accounts.
	 */
	@GetMapping(value = "/accounts")
	public List<Account> accountSummary() {
		return accountManager.getAllAccounts();
	}

	/**
	 * Provide the details of an account with the given id.
	 */
	@GetMapping(value = "/accounts/{id}")
	public Account accountDetails(@PathVariable int id) {
		return retrieveAccount(id);
	}

	/**
	 * Creates a new Account, setting its URL as the Location header on the
	 * response.
	 */
	@PostMapping(value = "/accounts")
	@ResponseStatus(HttpStatus.CREATED) // 201
	public ResponseEntity<Void> createAccount(@RequestBody Account newAccount) {
		Account account = accountManager.save(newAccount);
		return entityWithLocation(account.getId());
	}

	/**
	 * Returns the Beneficiary with the given name for the Account with the
	 * given id.
	 */
	@GetMapping(value = "/accounts/{accountId}/beneficiaries/{beneficiaryName}")
	public Beneficiary getBeneficiary(@PathVariable("accountId") int accountId,
			@PathVariable("beneficiaryName") String beneficiaryName) {
		try {
			return retrieveAccount(accountId).getBeneficiary(beneficiaryName);
		}
		catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}
	}

	/**
	 * Adds a Beneficiary with the given name to the Account with the given id,
	 * setting its URL as the Location header on the response.
	 */
	@PostMapping(value = "/accounts/{accountId}/beneficiaries")
	@ResponseStatus(HttpStatus.CREATED) // 201
	public ResponseEntity<Void> addBeneficiary(@PathVariable long accountId, @RequestBody String beneficiaryName) {
		accountManager.addBeneficiary(accountId, beneficiaryName);
		return entityWithLocation(beneficiaryName);
	}

	/**
	 * Removes the Beneficiary with the given name from the Account with the
	 * given id.
	 */
	@DeleteMapping(value = "/accounts/{accountId}/beneficiaries/{beneficiaryName}")
	@ResponseStatus(HttpStatus.NO_CONTENT) // 204
	public void removeBeneficiary(@PathVariable long accountId, @PathVariable String beneficiaryName) {
		Account account = accountManager.getAccount(accountId);
		Beneficiary deletedBeneficiary = account.getBeneficiary(beneficiaryName);

		Map<String, Percentage> allocationPercentages = new HashMap<>();

		// If we are removing the only beneficiary or the beneficiary has an
		// allocation of zero we don't need to worry. Otherwise, need to share
		// out the benefit of the deleted beneficiary amongst all the others
		if (account.getBeneficiaries().size() != 1
				&& (!deletedBeneficiary.getAllocationPercentage().equals(Percentage.zero()))) {
			// This logic is very simplistic, doesn't account for roundign errors
			Percentage p = deletedBeneficiary.getAllocationPercentage();
			int remaining = account.getBeneficiaries().size() - 1;
			double extra = p.asDouble() / remaining;

			for (Beneficiary beneficiary : account.getBeneficiaries()) {
				if (beneficiary != deletedBeneficiary) {
					double newValue = beneficiary.getAllocationPercentage().asDouble() + extra;
					allocationPercentages.put(beneficiary.getName(), new Percentage(newValue));
				}
			}
		}

		accountManager.removeBeneficiary(accountId, beneficiaryName, allocationPercentages);
	}

	/**
	 * Maps UnsupportedOperationException to a 501 Not Implemented HTTP status
	 * code.
	 */
	@ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
	@ExceptionHandler({ UnsupportedOperationException.class })
	public void handleUnableToReallocate(Exception ex) {
		log.error("Exception is: ", ex);
		// just return empty 501
	}

	/**
	 * Maps IllegalArgumentExceptions to a 404 Not Found HTTP status code.
	 */
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(IllegalArgumentException.class)
	public void handleNotFound(Exception ex) {
		log.error("Exception is: ", ex);
		// return empty 404
	}

	/**
	 * Maps DataIntegrityViolationException to a 409 Conflict HTTP status code.
	 */
	@ResponseStatus(HttpStatus.CONFLICT)
	@ExceptionHandler({ DataIntegrityViolationException.class })
	public void handleAlreadyExists(Exception ex) {
		log.error("Exception is: ", ex);
		// return empty 409
	}

	/**
	 * Finds the Account with the given id, throwing an IllegalArgumentException
	 * if there is no such Account.
	 */
	private Account retrieveAccount(long accountId) throws ResponseStatusException {
		Account account = accountManager.getAccount(accountId);
		if (account == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such account with id " + accountId);
		}
		return account;
	}

	/**
	 * Return a response with the location of the new resource. It's URL is
	 * assumed to be a child of the URL just received.
	 * <p>
	 * Suppose we have just received an incoming URL of, say,
	 * <code>http://localhost:8080/accounts</code> and <code>resourceId</code>
	 * is "12345". Then the URL of the new resource will be
	 * <code>http://localhost:8080/accounts/12345</code>.
	 *
	 * @param resourceId
	 *            Is of the new resource.
	 * @return
	 */
	private ResponseEntity<Void> entityWithLocation(Object resourceId) {

		// Determines URL of child resource based on the full URL of the given
		// request, appending the path info with the given resource Identifier
		URI location = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{childId}").buildAndExpand(resourceId)
				.toUri();

		// Return an HttpEntity object - it will be used to build the
		// HttpServletResponse
		return ResponseEntity.created(location).build();
	}

}
