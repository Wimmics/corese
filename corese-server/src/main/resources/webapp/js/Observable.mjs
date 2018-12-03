export class Observable {
  constructor() {
      this.observers = new Set();
  }

  addObserver(o) {
      if (! this.observers.has(o)) {
          this.observers.add(o);
      }
  }

  notififyObservers() {
      this.observers.forEach(
          o => o.update( this, undefined)
      )
  }

}