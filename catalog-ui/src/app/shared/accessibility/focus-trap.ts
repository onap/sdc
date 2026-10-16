const FOCUSABLE_SELECTOR = 'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])';

// An opener that was hidden meanwhile (e.g. a dashboard card's buttons, shown only while the card
// has focus) cannot take focus back; the nearest focusable ancestor keeps the user in place.
function restoreFocus(element: HTMLElement): void {
  let target: HTMLElement | null = element;
  while (target) {
    target.focus();
    if (document.activeElement === target) {
      return;
    }
    target = target.parentElement && target.parentElement.closest(FOCUSABLE_SELECTOR) as HTMLElement;
  }
}

export class FocusTrap {
  private previousFocus: HTMLElement | null = null;
  private container: HTMLElement | null = null;
  private boundKeydown: (e: KeyboardEvent) => void;

  constructor() {
    this.boundKeydown = this.handleKeydown.bind(this);
  }

  activate(container: HTMLElement): void {
    this.container = container;
    this.previousFocus = document.activeElement as HTMLElement;
    document.addEventListener('keydown', this.boundKeydown);

    const focusable = this.getFocusableElements();
    if (focusable.length > 0) {
      setTimeout(() => focusable[0].focus(), 0);
    }
  }

  deactivate(): void {
    document.removeEventListener('keydown', this.boundKeydown);
    if (this.previousFocus && this.previousFocus.focus) {
      restoreFocus(this.previousFocus);
    }
    this.container = null;
    this.previousFocus = null;
  }

  private handleKeydown(event: KeyboardEvent): void {
    if (!this.container) {
      return;
    }

    if (event.key === 'Tab' || event.keyCode === 9) {
      const focusable = this.getFocusableElements();
      if (focusable.length === 0) {
        event.preventDefault();
        return;
      }

      const first = focusable[0];
      const last = focusable[focusable.length - 1];

      if (event.shiftKey) {
        if (document.activeElement === first) {
          event.preventDefault();
          last.focus();
        }
      } else {
        if (document.activeElement === last) {
          event.preventDefault();
          first.focus();
        }
      }
    }
  }

  private getFocusableElements(): HTMLElement[] {
    if (!this.container) {
      return [];
    }
    const elements = this.container.querySelectorAll(FOCUSABLE_SELECTOR);
    return Array.prototype.slice.call(elements).filter(
      (el: HTMLElement) => el.offsetParent !== null
    );
  }
}
