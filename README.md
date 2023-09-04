# Order Book

[![Java CI with Maven](https://github.com/0xfauzi/order-book/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/0xfauzi/order-book/actions/workflows/maven.yml)

A basic implementation of an order book.

After building a jar, run with the command:
`java -cp order-book.jar com.0xfauzi.orderbook.Main -c=BTC-USD`


## Order book in action
https://github.com/0xfauzi/order-book/assets/5702728/6437b9dc-9d15-41ef-8144-ddc95c891378

While running, the order book prints detailed logs into a log file, making it easy to inspect events and the current state of the order book.
A sample log output can be found below

`| UPDATE_ORDER | Order book updated with a new order at an existing level: Order{id='2fc57b35-681b-4783-8e06-ebf42979827e'type='sell', price=19835.41, quantity=0.00010000}`\
`| UPDATE_ORDER | Order book updated with a new order at an existing level: Order{id='a4245053-0f7a-4b9c-a4cf-67f143605421'type='sell', price=19835.41, quantity=0.00010000}`\
`| ADD_LEVEL | New level added: Level{ price=19697.99 type=BUY orderCount=1 orderQuantity=0.00010000 orders=[Order{id='1a43a502-1d48-4ba6-b3c2-546377cc9f71'type='buy', price=19697.99, quantity=0.00010000}]}`\
`| ADD_LEVEL, ADD_ORDER | New buy order added at new level: Level{ price=19697.99 type=BUY orderCount=1 orderQuantity=0.00010000 orders=[Order{id='1a43a502-1d48-4ba6-b3c2-546377cc9f71'type='buy', price=19697.99, quantity=0.00010000}]}`\
`| UPDATE_ORDER | Order book updated with a new order at an existing level: Order{id='1a43a502-1d48-4ba6-b3c2-546377cc9f71'type='buy', price=19697.99, quantity=0.00010000}`\
`| UPDATE_ORDER | Order book updated with a new order at an existing level: Order{id='1a43a502-1d48-4ba6-b3c2-546377cc9f71'type='buy', price=19697.99, quantity=0.00010000}`\
`| UPDATE_ORDER | Order book updated with a new order at an existing level: Order{id='1a43a502-1d48-4ba6-b3c2-546377cc9f71'type='buy', price=19697.99, quantity=0.00010000}`\
`| UPDATE_ORDER | Order book updated with a new order at an existing level: Order{id='1a43a502-1d48-4ba6-b3c2-546377cc9f71'type='buy', price=19697.99, quantity=0.00010000}`\
`| UPDATE_ORDER | Order book updated with a new order at an existing level: Order{id='1a43a502-1d48-4ba6-b3c2-546377cc9f71'type='buy', price=19697.99, quantity=0.00010000}`\ 
`| UPDATE_ORDER | Order book updated with a new order at an existing level: Order{id='85937ac9-8bbb-4d3f-b644-b57bdbcfeb88'type='buy', price=19697.99, quantity=0.00010000}`\
`| REMOVE_ORDER | Cancel order received, removing from order book: Order{id='d850c519-3352-454d-959d-6157d93a310d'type='sell', price=20159.51, quantity=0E-8}`\
`| REMOVE_ORDER | Cancel order received, removing from order book: Order{id='2dcb9b20-57ca-4a89-a3e9-73cddc5167d0'type='sell', price=20597.37, quantity=0E-8}`\
`| ADD_LEVEL | New level added: Level{ price=19748.39 type=BUY orderCount=1 orderQuantity=0.00010000 orders=[Order{id='1acb3c32-7078-48a1-a977-74d2795c4201'type='buy', price=19748.39, quantity=0.00010000}]}`\
`| ADD_LEVEL, ADD_ORDER | New buy order added at new level: Level{ price=19748.39 type=BUY orderCount=1 orderQuantity=0.00010000 orders=[Order{id='1acb3c32-7078-48a1-a977-74d2795c4201'type='buy', price=19748.39, quantity=0.00010000}]}`\
`| UPDATE_ORDER | Order book updated with a new order at an existing level: Order{id='1acb3c32-7078-48a1-a977-74d2795c4201'type='buy', price=19748.39, quantity=0.00010000}`\
`| UPDATE_ORDER | Order book updated with a new order at an existing level: Order{id='1acb3c32-7078-48a1-a977-74d2795c4201'type='buy', price=19748.39, quantity=0.00010000}`\
`| UPDATE_ORDER | Order book updated with a new order at an existing level: Order{id='1acb3c32-7078-48a1-a977-74d2795c4201'type='buy', price=19748.39, quantity=0.00010000}`\
`| UPDATE_ORDER | Order book updated with a new order at an existing level: Order{id='1acb3c32-7078-48a1-a977-74d2795c4201'type='buy', price=19748.39, quantity=0.00010000}`\
`| UPDATE_ORDER | Order book updated with a new order at an existing level: Order{id='1acb3c32-7078-48a1-a977-74d2795c4201'type='buy', price=19748.39, quantity=0.00010000}`\
`| UPDATE_ORDER | Order book updated with a new order at an existing level: Order{id='46fb71f9-ac6d-4013-83bd-9786331c6487'type='buy', price=19748.39, quantity=0.00010000}`\
`| REMOVE_ORDER | Cancel order received, removing from order book: Order{id='f6f45d75-42d2-4894-af35-5f8ced21964f'type='sell', price=20020.54, quantity=0E-8}`\
`| ADD_LEVEL | New level added: Level{ price=20390.51 type=SELL orderCount=1 orderQuantity=0.00010000 orders=[Order{id='20b4ae7c-0b23-4795-92bb-9b91f74ba55b'type='sell', price=20390.51, quantity=0.00010000}]}`\
`| ADD_LEVEL, ADD_ORDER | New sell order added at new level: Level{ price=20390.51 type=SELL orderCount=1 orderQuantity=0.00010000 orders=[Order{id='20b4ae7c-0b23-4795-92bb-9b91f74ba55b'type='sell', price=20390.51, quantity=0.00010000}]}`\
`| UPDATE_ORDER | Order book updated with a new order at an existing level: Order{id='20b4ae7c-0b23-4795-92bb-9b91f74ba55b'type='sell', price=20390.51, quantity=0.00010000}`\
`| UPDATE_ORDER | Order book updated with a new order at an existing level: Order{id='20b4ae7c-0b23-4795-92bb-9b91f74ba55b'type='sell', price=20390.51, quantity=0.00010000}`\
`| UPDATE_ORDER | Order book updated with a new order at an existing level: Order{id='20b4ae7c-0b23-4795-92bb-9b91f74ba55b'type='sell', price=20390.51, quantity=0.00010000}`\
`| UPDATE_ORDER | Order book updated with a new order at an existing level: Order{id='20b4ae7c-0b23-4795-92bb-9b91f74ba55b'type='sell', price=20390.51, quantity=0.00010000}`\
`| UPDATE_ORDER | Order book updated with a new order at an existing level: Order{id='20b4ae7c-0b23-4795-92bb-9b91f74ba55b'type='sell', price=20390.51, quantity=0.00010000}`\
`| UPDATE_ORDER | Order book updated with a new order at an existing level: Order{id='20b4ae7c-0b23-4795-92bb-9b91f74ba55b'type='sell', price=20390.51, quantity=0.00010000}`\
`| UPDATE_ORDER | Order book updated with a new order at an existing level: Order{id='625a90da-1255-49d6-9001-40720c221e73'type='sell', price=20390.51, quantity=0.00010000}`
