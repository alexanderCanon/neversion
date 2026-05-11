import { Component } from '@angular/core';

@Component({
  selector: 'app-how-to-buy',
  templateUrl: './how-to-buy.component.html',
  styleUrls: ['./how-to-buy.component.css']
})
export class HowToBuyComponent {
  steps = [
    {
      title: 'Explora el Catálogo',
      description: 'Navega por nuestra amplia variedad de servicios de streaming, juegos y licencias. Haz clic en "Ver detalles" para conocer más sobre cada uno.',
      icon: 'bi-search'
    },
    {
      title: 'Elige tu Plan',
      description: 'Selecciona si deseas un Perfil Individual (más económico) o una Cuenta Completa (para toda la familia). Añade tus selecciones al carrito.',
      icon: 'bi-cart-plus'
    },
    {
      title: 'Regístrate o Inicia Sesión',
      description: 'Para garantizar la seguridad de tus accesos, necesitamos que crees una cuenta. Es rápido y solo te pedimos los datos básicos.',
      icon: 'bi-person-badge'
    },
    {
      title: 'Realiza tu Pago',
      description: 'Utiliza nuestras cuentas bancarias (BI, Banrural, GyT, BAC, BAM) o billeteras digitales como Fri y PayPal para realizar tu transferencia.',
      icon: 'bi-bank'
    },
    {
      title: 'Sube tu Comprobante',
      description: 'Toma una captura de pantalla de tu transferencia y súbela en la sección de pago. Nuestro equipo validará el depósito en un tiempo récord (15-45 min).',
      icon: 'bi-cloud-arrow-up'
    },
    {
      title: '¡Recibe tus Accesos!',
      description: 'Una vez validado, tus credenciales (email, clave y PIN) aparecerán automáticamente en tu Panel de Cliente. ¡A disfrutar!',
      icon: 'bi-key-fill'
    }
  ];
}
