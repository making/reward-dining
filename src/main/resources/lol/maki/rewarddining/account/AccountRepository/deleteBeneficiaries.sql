DELETE
FROM t_account_beneficiary
WHERE account_id = :accountId
  AND name NOT IN (:beneficiaryNames)