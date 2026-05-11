import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-support',
  templateUrl: './support.component.html',
  styleUrls: ['./support.component.css']
})
export class SupportComponent {
  private readonly router = inject(Router);

  supportChannels = [
    {
      title: 'WhatsApp Directo',
      description: 'Asistencia inmediata para problemas con tus cuentas o dudas sobre pagos.',
      action: 'Chatear ahora',
      icon: 'bi-whatsapp',
      color: '#25D366',
      link: 'https://wa.me/message/WEOAAOMZ5XU3I1'
    },
    {
      title: 'Reporte de Ticket',
      description: '¿Tienes un problema técnico persistente? Envíanos un reporte detallado.',
      action: 'Abrir Formulario',
      icon: 'bi-ticket-perforated',
      color: 'var(--primary-color)',
      route: '/contact'
    },
    {
      title: 'Correo Electrónico',
      description: 'Para consultas comerciales o propuestas de mayoristas.',
      action: 'Enviar Email',
      icon: 'bi-envelope-at',
      color: '#003087',
      link: 'mailto:soporte@neversion.com'
    }
  ];

  faqs = [
    {
      q: '¿Qué hago si mi cuenta no funciona?',
      a: 'Primero, verifica que estés ingresando los datos correctamente. Si el problema persiste, contacta a soporte vía WhatsApp con tu número de orden para una solución rápida.'
    },
    {
      q: '¿Cómo renuevo mi suscripción?',
      a: 'Desde tu Panel de Cliente, selecciona la suscripción vencida y haz clic en "Renovar". Sube tu comprobante y el sistema reactivará tu acceso.'
    },
    {
      q: '¿En cuánto tiempo recibo mi compra?',
      a: 'Una vez subido el comprobante, el tiempo de validación promedio es de 15 a 45 minutos dentro de nuestro horario de atención.'
    }
  ];

  handleAction(channel: any): void {
    if (channel.link) {
      window.open(channel.link, '_blank');
    } else if (channel.route) {
      this.router.navigate([channel.route]);
    }
  }
}
