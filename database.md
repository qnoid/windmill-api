The 'public' schema holds all windmill domain specific tables.
The 'sns' schema holds all Amazon SNS specific tables.
The 'apple' schema holds all Apple specific tables.

The relationship is one-directional as in
	'sns' -> 'public'
	'apple' -> 'public'
This allows the 'public' data to exist independently of the 'sns', 'apple' ones.

The 'secret' schema holds potentially data that is considered cryptographically secret.
