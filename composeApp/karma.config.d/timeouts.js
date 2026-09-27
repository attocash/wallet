// Migration tests perform several real, deliberately expensive Argon2 derivations.
config.client = config.client || {};
config.client.mocha = { ...config.client.mocha, timeout: 60000 };
