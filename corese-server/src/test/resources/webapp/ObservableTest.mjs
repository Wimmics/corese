import {Observable} from './js/Observable';
import {Observer} from './js/Observer';

class MyObserver extends Observer {
    update(observable, data) {
        super.update(observable, data);
        console.log("observer notified");
    }
}

class MyObservable extends Observable {

}

let observer = new MyObserver();
let observable = new MyObservable();
observable.addObserver(observer);
observable.notififyObservers();
