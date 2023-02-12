insert into payment_method(name,price_modifier_from,price_modifier_to,points_modifier) values ('CASH',0.9,1,0.05)  ON CONFLICT DO NOTHING;
insert into payment_method(name,price_modifier_from,price_modifier_to,points_modifier) values ('CASH_ON_DELIVERY',1,1.02,0.05)  ON CONFLICT DO NOTHING;
insert into payment_method(name,price_modifier_from,price_modifier_to,points_modifier) values ('VISA',0.95,1,0.03) ON CONFLICT DO NOTHING;
insert into payment_method(name,price_modifier_from,price_modifier_to,points_modifier) values ('MASTERCARD',0.95,1,0.03) ON CONFLICT DO NOTHING;
insert into payment_method(name,price_modifier_from,price_modifier_to,points_modifier) values ('AMEX',0.98,1.01,0.02) ON CONFLICT DO NOTHING;
insert into payment_method(name,price_modifier_from,price_modifier_to,points_modifier) values ('JCB',0.95,1,0.05) ON CONFLICT DO NOTHING;
