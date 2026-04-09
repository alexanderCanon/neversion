import { Injectable } from '@angular/core';

export interface LogEntry {
  timestamp: string;
  message: string;
  error: unknown;
  url?: string;
  method?: string;
}

@Injectable({ providedIn: 'root' })
export class ErrorLoggerService {
  private readonly STORAGE_KEY = 'neversion_error_log';
  private readonly MAX_LOGGED_ERRORS = 10;

  log(error: unknown, context?: { url?: string; method?: string }): void {
    const entry: LogEntry = {
      timestamp: new Date().toISOString(),
      message: this.extractMessage(error),
      error,
      url: context?.url,
      method: context?.method,
    };

    this.logToConsole(entry);
    this.saveToStorage(entry);
  }

  private extractMessage(error: unknown): string {
    if (error instanceof Error) {
      return error.message;
    }
    if (typeof error === 'string') {
      return error;
    }
    if (error && typeof error === 'object' && 'message' in error) {
      return String((error as { message: unknown }).message);
    }
    return 'Unknown error occurred';
  }

  private logToConsole(entry: LogEntry): void {
    console.error('[Neversion Error]', {
      timestamp: entry.timestamp,
      message: entry.message,
      url: entry.url,
      method: entry.method,
      error: entry.error,
    });
  }

  private saveToStorage(entry: LogEntry): void {
    try {
      const existing = this.getLog();
      const updated = [entry, ...existing].slice(0, this.MAX_LOGGED_ERRORS);
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(updated));
    } catch {
      console.warn('Failed to save error to localStorage');
    }
  }

  getLog(): LogEntry[] {
    try {
      const stored = localStorage.getItem(this.STORAGE_KEY);
      return stored ? JSON.parse(stored) : [];
    } catch {
      return [];
    }
  }

  clearLog(): void {
    localStorage.removeItem(this.STORAGE_KEY);
  }
}
