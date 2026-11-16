import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { catchError, finalize, of, switchMap } from 'rxjs';
import { AuthService } from './core/auth.service';
import { CatalogService } from './core/catalog.service';
import { Cart, MemberProfile, Notification, Order, Product } from './core/api.models';
import { CommerceService } from './core/commerce.service';

@Component({
  selector: 'ep-root', standalone: true, imports: [CurrencyPipe, DatePipe, FormsModule],
  template: `
    <div class="announcement">A little more considered. Always complimentary shipping over ₹2,500. <span>Explore the collection ↗</span></div>
    <header class="site-header">
      <a class="wordmark" href="#top" aria-label="Forma home">forma<span>®</span></a>
      <nav class="main-nav" aria-label="Main navigation">
        <a href="#collection" class="active">Shop all</a><a href="#collection" (click)="pickCategory('Objects')">Objects</a><a href="#collection" (click)="pickCategory('Home')">Home</a><a href="#story">Our story</a>
      </nav>
      <div class="header-actions"><button class="icon-button search-button" aria-label="Jump to search" (click)="focusSearch()">⌕</button>@if (auth.signedIn()) { <button class="account-link orders-link" (click)="openOrders()">Orders</button><button class="account-link" (click)="openMember()">Member</button> }<button class="account-link" (click)="openAuth()">{{ auth.signedIn() ? initials() : 'Sign in' }}</button><button class="bag-button" (click)="cartOpen.set(!cartOpen())">Bag <span>{{ cartCount() }}</span></button><button class="mobile-menu" aria-label="Open navigation">☰</button></div>
    </header>

    <main id="top">
      <section class="hero" aria-label="Featured collection">
        <div class="hero-photo"></div><div class="hero-shade"></div>
        <div class="hero-content"><p class="eyebrow light">THE EVERYDAY, RECONSIDERED</p><h1>Room to<br><em>live well.</em></h1><p class="hero-copy">Objects with a point of view.<br>Made for the way you actually live.</p><a class="light-cta" href="#collection">Shop the collection <span>↗</span></a></div>
        <div class="hero-caption"><span>01 / 04</span><span>Objects for slow mornings</span><span>⌖ 27° 10′ 12″ N</span></div>
        <span class="hero-index">A STUDY IN EVERYDAY</span>
      </section>

      <section class="intro" id="story"><p class="eyebrow">GOOD THINGS, MADE TO STAY</p><h2>Less, but with <em>feeling.</em></h2><p>Small-batch finds and thoughtful essentials that make a place feel a little more like yours.</p></section>

      <section class="catalog" id="collection">
        <div class="catalog-head"><div><p class="eyebrow">THE FORMA EDIT · 01—24</p><h2>The collection<span class="catalog-count">{{ filteredProducts().length.toString().padStart(2,'0') }}</span></h2></div><div class="catalog-tools"><label class="search-field"><span>⌕</span><input #searchBox type="search" placeholder="Find something lovely…" [ngModel]="searchTerm()" (ngModelChange)="searchTerm.set($event)" aria-label="Search products"></label><button class="sort-button" (click)="sortNewest.set(!sortNewest())">{{ sortNewest() ? 'Recently added ↓' : 'Featured ↕' }}</button></div></div>
        <div class="category-tabs" aria-label="Filter categories"><button [class.selected]="selectedCategory() === 'All'" (click)="pickCategory('All')">Everything</button>@for (category of categories(); track category) { <button [class.selected]="selectedCategory() === category" (click)="pickCategory(category)">{{ category }}</button> }</div>

        @if (loading()) { <div class="status-panel"><span class="spinner"></span><p>Finding the good things…</p></div> }
        @else if (!auth.signedIn()) { <div class="status-panel"><p class="error-mark">✳</p><h3>Good things are just around the corner.</h3><p>Sign in to see the collection and find your next favourite.</p><button class="text-link" (click)="openAuth()">Sign in to explore ↗</button></div> }
        @else if (error()) { <div class="status-panel"><p class="error-mark">↗</p><h3>The collection is taking a moment.</h3><p>{{ error() }}</p><button class="text-link" (click)="loadProducts()">Try again ↗</button></div> }
        @else if (!filteredProducts().length) { <div class="status-panel"><p class="error-mark">⌕</p><h3>Nothing on this shelf yet.</h3><p>Try another search, or explore everything.</p><button class="text-link" (click)="clearFilters()">Clear filters ↗</button></div> }
        @else {
          <div class="product-grid">@for (product of filteredProducts(); track product.id; let i = $index) {
            <article class="product-card">
              <div class="product-image" [class]="'product-image tone-' + (i % 6)" [style.background-image]="'url(' + imageFor(product.category, i) + ')'">
                <span class="image-label">{{ product.category }}</span><button class="quick-add" (click)="addToBag(product)" [attr.aria-label]="'Add ' + product.name + ' to bag'">+</button>
                @if (i % 4 === 0) { <span class="product-badge">A FORMA FAVOURITE</span> }
              </div>
              <div class="product-details"><div><h3>{{ product.name }}</h3><p>{{ product.description || 'A thoughtful find for everyday living.' }}</p></div><span class="price">{{ product.price | currency:'INR':'symbol-narrow':'1.0-0' }}</span></div>
            </article>
          }</div>
        }
        <div class="catalog-foot"><span>Showing {{ filteredProducts().length }} considered finds</span><span>Made for keeps, never for landfill.</span></div>
      </section>

      <section class="newsletter"><div class="newsletter-stamp">f.</div><p class="eyebrow">A NOTE FROM US, NOW AND THEN</p><h2>A little more <em>thoughtful.</em></h2><p>New finds, studio notes, and small ways to make home feel good.</p><form (submit)="$event.preventDefault(); subscribed.set(true)"><input type="email" placeholder="Your email address" aria-label="Your email address" required><button type="submit">{{ subscribed() ? 'You’re on the list ✓' : 'Count me in ↗' }}</button></form><small>Just the good stuff. Unsubscribe whenever.</small></section>
    </main>

    <footer class="site-footer"><a class="wordmark footer-mark" href="#top">forma<span>®</span></a><span>Objects for the everyday. Made with a little more feeling.</span><div><a href="#collection">Instagram ↗</a><a href="#story">Get in touch ↗</a></div><small>© FORMA 2025</small></footer>

    @if (cartOpen()) { <div class="scrim" (click)="cartOpen.set(false)"></div><aside class="cart-drawer"><div class="drawer-head"><div><p class="eyebrow">YOUR LITTLE EDIT</p><h2>The bag <span>({{ cartCount() }})</span></h2></div><button class="close-button" (click)="cartOpen.set(false)" aria-label="Close bag">×</button></div>
      @if (!cart().length) { <div class="empty-cart"><span>✳</span><h3>A little room for something lovely.</h3><p>Your bag is waiting for its first find.</p><button class="dark-button" (click)="cartOpen.set(false)">Explore the collection ↗</button></div> }
      @else { <div class="cart-lines">@for (line of cart(); track line.product.id) { <div class="cart-line"><div class="cart-thumb" [style.background-image]="'url(' + imageFor(line.product.category, 0) + ')'" ></div><div class="cart-line-info"><h3>{{ line.product.name }}</h3><span>{{ line.product.price | currency:'INR':'symbol-narrow':'1.0-0' }}</span><div class="quantity"><button (click)="changeQty(line.product.id,-1)">−</button><span>{{ line.quantity }}</span><button (click)="changeQty(line.product.id,1)">+</button><button class="remove-item" (click)="removeFromCart(line.product.id)" aria-label="Remove item">×</button></div></div></div> }</div><div class="cart-total"><span>Subtotal</span><strong>{{ cartTotal() | currency:'INR':'symbol-narrow':'1.0-0' }}</strong><p>Final prices and stock are confirmed at checkout.</p><button class="dark-button" [disabled]="checkoutBusy()" (click)="checkout()">{{ checkoutBusy() ? 'Placing your order…' : 'Place order ↗' }}</button>@if (checkoutError()) { <small class="checkout-note">{{ checkoutError() }}</small> }</div> }
    </aside> }

    @if (ordersOpen()) { <div class="modal-backdrop" (click)="ordersOpen.set(false)"><section class="orders-modal" role="dialog" aria-modal="true" aria-labelledby="orders-title" (click)="$event.stopPropagation()"><button class="close-button modal-close" (click)="ordersOpen.set(false)" aria-label="Close">×</button><p class="eyebrow">A LITTLE HISTORY</p><h2 id="orders-title">Your orders</h2>@if (paymentNotice()) { <p class="form-error payment-notice">{{ paymentNotice() }}</p> }@if (ordersLoading()) { <p class="auth-intro">Gathering your orders…</p> }@else if (!orders().length) { <p class="auth-intro">Your first order is just around the corner.</p> }@else { <div class="orders-list">@for (order of orders(); track order.id) { <article class="order-card"><div class="order-card-head"><div><span class="order-number">ORDER · {{ order.id.slice(0,8).toUpperCase() }}</span><p>{{ order.createdAt | date:'mediumDate' }}</p></div><div class="order-actions"><span class="order-status">{{ order.status }}</span>@if (order.status === 'PLACED' || order.status === 'PAYMENT_PENDING') { <button class="pay-order-button" [disabled]="payingOrderId() === order.id" (click)="payExistingOrder(order)">{{ payingOrderId() === order.id ? 'Processing…' : 'Complete demo payment ↗' }}</button> }</div></div><div class="order-card-items">@for (item of order.items; track item.productId) { <span>{{ item.quantity }} × {{ item.productName }}</span> }</div><div class="order-card-total"><span>Total</span><strong>{{ order.totalAmount | currency:'INR':'symbol-narrow':'1.0-0' }}</strong></div></article> }</div> }<p class="eyebrow updates-label">ORDER UPDATES</p>@if (!notifications().length) { <p class="auth-intro">No new notes just yet.</p> }@for (note of notifications(); track note.id) { <button class="notification-row" [class.unread]="!note.read" (click)="markNotificationRead(note)"><span>{{ note.message }}</span><small>{{ note.createdAt | date:'mediumDate' }} · {{ note.read ? 'Read' : 'Mark read' }}</small></button> }</section></div> }
    @if (memberOpen()) { <div class="modal-backdrop" (click)="memberOpen.set(false)"><section class="orders-modal member-modal" role="dialog" aria-modal="true" aria-labelledby="member-title" (click)="$event.stopPropagation()"><button class="close-button modal-close" (click)="memberOpen.set(false)" aria-label="Close">×</button><p class="eyebrow">YOUR MEMBERSHIP</p><h2 id="member-title">A little more you.</h2>@if (memberProfile()) { <div class="member-summary"><span>{{ memberProfile()?.memberNumber }}</span><strong>{{ memberProfile()?.tier }} · {{ memberProfile()?.status }}</strong></div> }<p class="auth-intro">Keep your contact details up to date.</p><form class="auth-form" (submit)="saveMember($event)"><label>First name<input name="memberFirstName" [ngModel]="memberFirstName()" (ngModelChange)="memberFirstName.set($event)" autocomplete="given-name" required></label><label>Last name<input name="memberLastName" [ngModel]="memberLastName()" (ngModelChange)="memberLastName.set($event)" autocomplete="family-name" required></label><label>Phone number<input name="phoneNumber" type="tel" [ngModel]="memberPhone()" (ngModelChange)="memberPhone.set($event)" autocomplete="tel"></label>@if (memberError()) { <p class="form-error">{{ memberError() }}</p> }@if (memberNotice()) { <p class="form-success">{{ memberNotice() }}</p> }<button class="dark-button" [disabled]="memberBusy()">{{ memberBusy() ? 'Saving…' : 'Save member details ↗' }}</button></form></section></div> }    @if (authOpen()) { <div class="modal-backdrop" (click)="authOpen.set(false)"><section class="auth-modal" role="dialog" aria-modal="true" aria-labelledby="auth-title" (click)="$event.stopPropagation()"><button class="close-button modal-close" (click)="authOpen.set(false)" aria-label="Close">×</button><a class="wordmark modal-mark" href="#top">forma<span>®</span></a><p class="eyebrow">A SEAT AT OUR TABLE</p><h2 id="auth-title">{{ registering() ? 'Come on in.' : 'Good to have you.' }}</h2><p class="auth-intro">{{ registering() ? 'Make a little space for good things.' : 'Sign in to save the things you love.' }}</p>
        <form class="auth-form" (submit)="submitAuth($event)">@if (registering()) { <div class="name-fields"><label>First name<input name="firstName" [ngModel]="firstName()" (ngModelChange)="firstName.set($event)" autocomplete="given-name" required></label><label>Last name<input name="lastName" [ngModel]="lastName()" (ngModelChange)="lastName.set($event)" autocomplete="family-name" required></label></div> }<label>Email address<input name="email" type="email" [ngModel]="emailInput()" (ngModelChange)="emailInput.set($event)" autocomplete="email" required></label><label>Password<input name="password" type="password" [ngModel]="passwordInput()" (ngModelChange)="passwordInput.set($event)" [minlength]="registering() ? 7 : 1" [maxlength]="registering() ? 30 : 100" [autocomplete]="registering() ? 'new-password' : 'current-password'" required></label>@if (authError()) { <p class="form-error">{{ authError() }}</p> }@if (authNotice()) { <p class="form-success">{{ authNotice() }}</p> }<button class="dark-button" [disabled]="authBusy()">{{ authBusy() ? 'One moment…' : registering() ? 'Create account ↗' : 'Sign in ↗' }}</button></form>
        <button class="switch-auth" (click)="registering.set(!registering()); authError.set(''); authNotice.set('')">{{ registering() ? 'Already part of the family? Sign in' : 'New around here? Create an account' }}</button></section></div> }
  `
})
export class AppComponent implements OnInit {
  readonly auth = inject(AuthService); private readonly catalog = inject(CatalogService); private readonly commerce = inject(CommerceService);
  readonly products = signal<Product[]>([]); readonly loading = signal(true); readonly error = signal('');
  readonly selectedCategory = signal('All'); readonly searchTerm = signal(''); readonly sortNewest = signal(false);
  readonly cart = signal<{ product: Product; quantity: number }[]>([]); readonly cartOpen = signal(false);
  readonly checkoutBusy = signal(false); readonly checkoutError = signal(''); readonly paymentNotice = signal(''); readonly payingOrderId = signal(''); readonly ordersOpen = signal(false); readonly orders = signal<Order[]>([]); readonly ordersLoading = signal(false); readonly notifications = signal<Notification[]>([]);
  readonly memberOpen = signal(false); readonly memberProfile = signal<MemberProfile | null>(null); readonly memberBusy = signal(false); readonly memberError = signal(''); readonly memberNotice = signal(''); readonly memberFirstName = signal(''); readonly memberLastName = signal(''); readonly memberPhone = signal('');
  readonly authOpen = signal(false); readonly registering = signal(false); readonly authBusy = signal(false); readonly authError = signal('');
  readonly emailInput = signal(''); readonly passwordInput = signal(''); readonly firstName = signal(''); readonly lastName = signal('');
  readonly authNotice = signal('');
  readonly subscribed = signal(false);
  readonly categories = computed(() => [...new Set(this.products().map((product) => product.category))].sort((a,b) => a.localeCompare(b)));
  readonly filteredProducts = computed(() => {
    const term = this.searchTerm().trim().toLocaleLowerCase(); const category = this.selectedCategory();
    const results = this.products().filter((product) => (category === 'All' || product.category === category) && (!term || `${product.name} ${product.category} ${product.description ?? ''} ${product.sku}`.toLocaleLowerCase().includes(term)));
    return this.sortNewest() ? [...results].sort((a,b) => b.createdAt.localeCompare(a.createdAt)) : results;
  });
  readonly cartCount = computed(() => this.cart().reduce((total, line) => total + line.quantity, 0));
  readonly cartTotal = computed(() => this.cart().reduce((total, line) => total + line.product.price * line.quantity, 0));

  ngOnInit(): void { if (this.auth.signedIn()) this.loadProducts(); else this.loading.set(false); }
  loadProducts(): void {
    this.loading.set(true); this.error.set('');
    this.catalog.list().pipe(finalize(() => this.loading.set(false))).subscribe({ next: (products) => { this.products.set(products); this.loadCart(); }, error: (e: unknown) => this.error.set(this.message(e, 'We couldn’t reach the collection. Check that the API is running and try again.')) });
  }
  pickCategory(category: string): void { this.selectedCategory.set(category); }
  clearFilters(): void { this.selectedCategory.set('All'); this.searchTerm.set(''); }
  focusSearch(): void { document.querySelector<HTMLInputElement>('.search-field input')?.focus(); document.querySelector('#collection')?.scrollIntoView({ behavior: 'smooth' }); }
  imageFor(_category: string, index: number): string {
    const category = _category.toLowerCase();
    const photoIds = category.includes('light') ? ['1507473885765-e6ed057f782c','1513506003901-1e6a229e2d15']
      : category.includes('textile') ? ['1600210492486-724fe5c67fb0','1616486338812-3dadae4b4ace']
      : category.includes('kitchen') || category.includes('home') ? ['1603199506016-b9a594b593c0','1600210492486-724fe5c67fb0']
      : ['1610701596007-11502861dcfa','1578749556568-bc2c40e68b61','1490312278390-ab64016e0aa9'];
    return `https://images.unsplash.com/photo-${photoIds[index % photoIds.length]}?auto=format&fit=crop&w=920&q=85`;
  }
  loadCart(): void { this.commerce.getCart().subscribe({ next: (cart) => this.applyCart(cart), error: () => this.cart.set([]) }); }
  private applyCart(cart: Cart): void { const products = this.products(); this.cart.set(cart.items.flatMap((item) => { const product = products.find((entry) => entry.id === item.productId); return product ? [{ product, quantity: item.quantity }] : []; })); }
  addToBag(product: Product): void { this.commerce.addItem(product.id).subscribe({ next: (cart) => { this.applyCart(cart); this.checkoutError.set(''); this.cartOpen.set(true); }, error: (e: unknown) => { this.checkoutError.set(this.message(e, 'Could not add this item. Please try again.')); this.cartOpen.set(true); } }); }
  changeQty(id: string, change: number): void { const line = this.cart().find((entry) => entry.product.id === id); if (!line) return; const quantity = line.quantity + change; if (quantity < 1) { this.removeFromCart(id); return; } this.commerce.setQuantity(id, quantity).subscribe({ next: (cart) => this.applyCart(cart), error: (e: unknown) => this.checkoutError.set(this.message(e, 'Could not update your bag.')) }); }
  removeFromCart(id: string): void { this.commerce.removeItem(id).subscribe({ next: (cart) => this.applyCart(cart), error: (e: unknown) => this.checkoutError.set(this.message(e, 'Could not remove this item.')) }); }
  checkout(): void {
    this.checkoutBusy.set(true); this.checkoutError.set(''); this.paymentNotice.set('');
    this.commerce.checkout().pipe(
      switchMap((order) => this.commerce.payOrder(order.id).pipe(
        switchMap(() => this.commerce.getOrder(order.id)),
        catchError((error: unknown) => { this.paymentNotice.set(`Order placed, but payment is still pending: ${this.message(error, 'Please complete payment from your order below.')}`); return of(order); })
      )),
      finalize(() => this.checkoutBusy.set(false))
    ).subscribe({
      next: (order) => { this.cart.set([]); this.orders.update((items) => [order, ...items.filter((item) => item.id !== order.id)]); this.cartOpen.set(false); this.ordersOpen.set(true); },
      error: (e: unknown) => this.checkoutError.set(this.message(e, 'Checkout could not be completed. Your bag is saved; please try again.'))
    });
  }
  payExistingOrder(order: Order): void {
    this.payingOrderId.set(order.id); this.paymentNotice.set('');
    this.commerce.payOrder(order.id).pipe(switchMap(() => this.commerce.getOrder(order.id)), finalize(() => this.payingOrderId.set(''))).subscribe({
      next: (updated) => this.orders.update((items) => items.map((item) => item.id === updated.id ? updated : item)),
      error: (error: unknown) => this.paymentNotice.set(this.message(error, 'Payment is still pending. Please try again.'))
    });
  }
  openMember(): void {
    this.memberOpen.set(true); this.memberBusy.set(true); this.memberError.set(''); this.memberNotice.set('');
    this.commerce.getMemberProfile().pipe(finalize(() => this.memberBusy.set(false))).subscribe({
      next: (profile) => { this.memberProfile.set(profile); this.memberFirstName.set(profile.firstName); this.memberLastName.set(profile.lastName); this.memberPhone.set(profile.phoneNumber ?? ''); },
      error: (error: unknown) => { this.memberProfile.set(null); this.memberFirstName.set(''); this.memberLastName.set(''); this.memberPhone.set(''); if ((error as { status?: number }).status !== 404) this.memberError.set(this.message(error, 'Member details could not be loaded.')); }
    });
  }
  saveMember(event: Event): void {
    event.preventDefault(); this.memberBusy.set(true); this.memberError.set(''); this.memberNotice.set('');
    this.commerce.saveMemberProfile({ firstName: this.memberFirstName(), lastName: this.memberLastName(), phoneNumber: this.memberPhone() || null }).pipe(finalize(() => this.memberBusy.set(false))).subscribe({
      next: (profile) => { this.memberProfile.set(profile); this.memberNotice.set('Your member details are saved.'); },
      error: (error: unknown) => this.memberError.set(this.message(error, 'Your member details could not be saved.'))
    });
  }
  openOrders(): void { this.ordersOpen.set(true); this.ordersLoading.set(true); this.commerce.listOrders().pipe(finalize(() => this.ordersLoading.set(false))).subscribe({ next: (orders) => this.orders.set(orders), error: () => this.ordersLoading.set(false) }); this.commerce.notifications().subscribe({ next: (items) => this.notifications.set(items) }); }
  markNotificationRead(note: Notification): void { if (note.read) return; this.commerce.markRead(note.id).subscribe({ next: (updated) => this.notifications.update((items) => items.map((item) => item.id === updated.id ? updated : item)) }); }
  initials(): string { return this.auth.email().slice(0, 2).toUpperCase(); }
  openAuth(): void { if (this.auth.signedIn()) { this.auth.logout().subscribe({ next: () => this.auth.clear(), error: () => this.auth.clear() }); } else { this.authOpen.set(true); this.authError.set(''); } }
  submitAuth(event: Event): void {
    event.preventDefault(); this.authBusy.set(true); this.authError.set(''); this.authNotice.set('');
    const request = this.registering()
      ? this.auth.register({ email: this.emailInput(), password: this.passwordInput(), firstName: this.firstName(), lastName: this.lastName() })
      : this.auth.login(this.emailInput(), this.passwordInput());
    request.pipe(finalize(() => this.authBusy.set(false))).subscribe({
      next: () => { if (this.registering()) { this.registering.set(false); this.passwordInput.set(''); this.authNotice.set('Your account is ready. Sign in to continue.'); } else { this.authOpen.set(false); this.passwordInput.set(''); this.authError.set(''); this.loadProducts(); } },
      error: (e: unknown) => this.authError.set(this.message(e, 'That didn’t work. Please check your details and try again.'))
    });
  }
  private message(error: unknown, fallback: string): string {
    const candidate = error as { error?: { message?: string; data?: { message?: string } } };
    return candidate.error?.message ?? candidate.error?.data?.message ?? fallback;
  }
}


