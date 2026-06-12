import { Component, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface FaqItem {
  question: string;
  answer: string;
  category: string;
  isOpen: boolean;
}

interface GlossaryItem {
  term: string;
  definition: string;
  icon: string;
}

@Component({
  selector: 'app-help',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './help.component.html',
  styleUrls: ['./help.component.scss']
})
export class HelpComponent {
  // Navigation / Tabs
  activeTab = signal<'faq' | 'glossary' | 'workflow' | 'support'>('faq');

  // Search query
  searchQuery = signal<string>('');

  // Support Form State
  supportName = signal<string>('');
  supportEmail = signal<string>('');
  supportMessage = signal<string>('');
  supportSuccess = signal<boolean>(false);

  // FAQ Data
  faqs = signal<FaqItem[]>([
    {
      question: '¿Cómo agrego una Cuenta Maestra?',
      answer: 'Ve a la sección "Cuentas", haz clic en el botón "Nueva Cuenta Maestra", selecciona el servicio correspondiente, ingresa las credenciales de acceso (correo, contraseña) y define el límite de perfiles permitidos para ese servicio.',
      category: 'accounts',
      isOpen: false
    },
    {
      question: '¿Qué pasa al hacer clic en el botón "Eliminar esta cuenta"?',
      answer: 'La cuenta maestra NO se borra físicamente de forma inmediata de la base de datos si tiene suscripciones o perfiles activos asignados a clientes. El sistema la marca como inactiva/deshabilitada para que no se vendan nuevos perfiles de ella, pero preserva los accesos de los clientes que tengan suscripciones activas vinculadas hasta que expiren. Si la cuenta no tiene ningún perfil en uso, se inactiva por completo de forma segura.',
      category: 'accounts',
      isOpen: false
    },
    {
      question: '¿Por qué me aparece el error "maxProfiles reached"?',
      answer: 'Este error se produce porque has alcanzado la cantidad máxima de perfiles configurados para ese servicio específico (por ejemplo, máximo 7 perfiles en Netflix). El sistema valida esta cantidad antes de generar nuevos perfiles o de permitir la generación rápida, para evitar sobrepasar los límites de visualización simultánea de la plataforma de streaming.',
      category: 'profiles',
      isOpen: false
    },
    {
      question: '¿Cómo se entregan los accesos (correo, contraseña, PIN) al cliente?',
      answer: 'Una vez que un cliente realiza una compra en la tienda (storefront) y tú validas su pago en la sección de "Órdenes", el sistema asocia de forma automática un perfil disponible de la cuenta maestra al cliente. Le asigna un PIN de seguridad y marca la suscripción como activa. El cliente podrá ver estos accesos desde su propio portal o recibir la notificación correspondiente.',
      category: 'profiles',
      isOpen: false
    },
    {
      question: '¿Cuánto tiempo dura una "Reserva" de perfil?',
      answer: 'Cuando un cliente inicia una orden, el perfil correspondiente se coloca en estado "RESERVADO" para evitar que otro cliente lo compre al mismo tiempo. Si la orden no es pagada y validada dentro del periodo de vencimiento establecido, el sistema liberará la reserva automáticamente, devolviendo el perfil al inventario disponible.',
      category: 'workflow',
      isOpen: false
    },
    {
      question: '¿Cómo puedo renovar la suscripción de un cliente?',
      answer: 'Para renovar la suscripción de un cliente, ve a la sección "Suscripciones", busca la suscripción del cliente y haz clic en "Renovar". Podrás definir el nuevo periodo de validez. También puedes validarlo mediante una nueva orden de renovación que el cliente realice desde la tienda.',
      category: 'workflow',
      isOpen: false
    }
  ]);

  // Glossary Data
  glossary = signal<GlossaryItem[]>([
    {
      term: 'Cuenta Maestra',
      definition: 'La cuenta principal adquirida directamente en un proveedor de streaming o servicio digital (ej. Netflix, Disney+, Spotify). Es la que provee los perfiles individuales que posteriormente se comercializan.',
      icon: 'bi-wallet2'
    },
    {
      term: 'Perfil',
      definition: 'Subcuenta de acceso individual dentro de una Cuenta Maestra. Cada perfil cuenta con su propio nombre, PIN personalizado y es asignado de forma exclusiva a un cliente único.',
      icon: 'bi-person-badge'
    },
    {
      term: 'Reserva',
      definition: 'Bloqueo temporal de un perfil de servicio a favor de un cliente que ha iniciado una orden de compra en la tienda. Garantiza que el perfil no sea asignado a nadie más mientras se completa el pago.',
      icon: 'bi-calendar-check'
    },
    {
      term: 'Suscripción',
      definition: 'Relación comercial activa entre un cliente y un perfil asignado de una cuenta maestra, sujeta a un rango de fechas con su respectivo vencimiento y costo recurrente.',
      icon: 'bi-card-checklist'
    },
    {
      term: 'Orden',
      definition: 'El registro de una solicitud de compra o renovación efectuada por un cliente en el storefront. Se mantiene en estado pendiente de validación hasta que el vendedor confirme el pago.',
      icon: 'bi-receipt'
    },
    {
      term: 'Servicio',
      definition: 'El producto digital que se ofrece en la plataforma (ej. Netflix Premium, Prime Video, Spotify Family) que cuenta con sus propios parámetros de límites de perfiles, duraciones y precios.',
      icon: 'bi-box-seam'
    }
  ]);

  // Computed FAQs based on search filter
  filteredFaqs = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    if (!query) return this.faqs();
    return this.faqs().filter(faq => 
      faq.question.toLowerCase().includes(query) || 
      faq.answer.toLowerCase().includes(query) || 
      faq.category.toLowerCase().includes(query)
    );
  });

  // Computed Glossary based on search filter
  filteredGlossary = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    if (!query) return this.glossary();
    return this.glossary().filter(item => 
      item.term.toLowerCase().includes(query) || 
      item.definition.toLowerCase().includes(query)
    );
  });

  // Toggle FAQ collapse state
  toggleFaq(faq: FaqItem): void {
    this.faqs.update(items => 
      items.map(item => 
        item.question === faq.question ? { ...item, isOpen: !item.isOpen } : item
      )
    );
  }

  // Handle Tab Switch
  selectTab(tab: 'faq' | 'glossary' | 'workflow' | 'support'): void {
    this.activeTab.set(tab);
    // Clear search query when switching tabs to ensure all content is visible
    this.searchQuery.set('');
  }

  // Handle Support Form submission
  onSubmitSupport(event: Event): void {
    event.preventDefault();
    const name = this.supportName().trim();
    const email = this.supportEmail().trim();
    const message = this.supportMessage().trim();

    if (!name || !email || !message) return;
    
    // Build WhatsApp message format
    const formattedMessage = `Hola Alexander, mi nombre es ${name} (${email}). Escribo desde el Panel de Neversion para realizar la siguiente consulta:\n\n${message}`;
    const encodedMessage = encodeURIComponent(formattedMessage);
    const whatsappUrl = `https://wa.me/50258550420?text=${encodedMessage}`;
    
    // Open WhatsApp in a new tab
    window.open(whatsappUrl, '_blank');
    
    // Set success indicator
    this.supportSuccess.set(true);
    
    // Clear the message box but keep name and email for convenience
    this.supportMessage.set('');
    
    setTimeout(() => {
      this.supportSuccess.set(false);
    }, 5000);
  }
}
