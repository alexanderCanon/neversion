interface StoredSupabaseSession {
  access_token?: unknown;
}

export function getSupabaseAccessToken(storage: Storage): string | null {
  const authKey = findSupabaseAuthKey(storage);
  if (!authKey) {
    return null;
  }

  const storedSession = storage.getItem(authKey);
  if (!storedSession) {
    return null;
  }

  const parsedSession = JSON.parse(storedSession) as unknown;
  if (!isStoredSupabaseSession(parsedSession)) {
    return null;
  }

  return typeof parsedSession.access_token === 'string'
    ? parsedSession.access_token
    : null;
}

export function clearSupabaseSession(storage: Storage): void {
  const authKey = findSupabaseAuthKey(storage);
  if (authKey) {
    storage.removeItem(authKey);
  }
}

function findSupabaseAuthKey(storage: Storage): string | null {
  for (let index = 0; index < storage.length; index += 1) {
    const key = storage.key(index);
    if (key?.startsWith('sb-') && key.endsWith('-auth-token')) {
      return key;
    }
  }

  return null;
}

function isStoredSupabaseSession(value: unknown): value is StoredSupabaseSession {
  return typeof value === 'object' && value !== null && 'access_token' in value;
}
