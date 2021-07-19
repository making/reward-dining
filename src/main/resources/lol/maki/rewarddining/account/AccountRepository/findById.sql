select a.id                    as id,
       a.number                as account_number,
       a.name                  as account_name,
       c.number                as credit_card_number,
       b.id                    as beneficiary_id,
       b.name                  as beneficiary_name,
       b.allocation_percentage as beneficiary_allocation_percentage,
       b.savings               as beneficiary_savings
from t_account a
         left outer join t_account_beneficiary b ON a.id = b.account_id
         left outer join t_account_credit_card c ON a.id = c.account_id
where a.id = ?