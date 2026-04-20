import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'phone',
  standalone: true
})
export class PhonePipe implements PipeTransform {
  transform(value: string | null | undefined): string {
    if (!value) return '';
    
    // Remove all non-digit characters except '+'
    const cleanStr = value.replace(/[^\d+]/g, '');
    
    // If it already starts with a country code (+), format it as is or leave it
    if (cleanStr.startsWith('+')) {
      return cleanStr;
    }
    
    // Default fallback to +502 for Guatemalan numbers
    return `+502 ${cleanStr}`;
  }
}
