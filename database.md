The 'public' schema holds all windmill domain specific tables.
The 'sns' schema holds all Amazon SNS specific tables.

The relationship is one-directional as in
	'sns' -> 'public'
This allows the 'public' data to exist independently of the 'sns' one.
