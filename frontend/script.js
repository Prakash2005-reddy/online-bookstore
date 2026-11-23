const API_HOST = location.hostname === "127.0.0.1" ? "127.0.0.1" : "localhost";
const API_BASE = `http://${API_HOST}:8080/api`;
const state = { books: [], categories: [], cart: null, user: null, query: "", categoryId: "", authMode: "login", toastTimer: null };
const $ = (selector, root = document) => root.querySelector(selector);
const $$ = (selector, root = document) => [...root.querySelectorAll(selector)];
const money = (amount) => new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" }).format(Number(amount || 0));
const escapeHtml = (value) => String(value ?? "").replace(/[&<>"']/g, (char) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[char]);

async function api(path, options = {}) {
  const headers = new Headers(options.headers || {});
  if (options.body && !headers.has("Content-Type")) headers.set("Content-Type", "application/json");
  if (options.method && !["GET", "HEAD", "OPTIONS"].includes(options.method.toUpperCase())) {
    const token = csrfToken || readCookie("XSRF-TOKEN");
    if (token) headers.set("X-XSRF-TOKEN", token);
  }
  const response = await fetch(`${API_BASE}${path}`, { ...options, headers, credentials: "include" });
  if (response.status === 204) return null;
  const payload = await response.json().catch(() => null);
  if (!response.ok) {
    const message = payload?.message || payload?.error || (response.status === 401 ? "Please log in to continue." : `Request failed (${response.status}).`);
    const error = new Error(message);
    error.status = response.status;
    throw error;
  }
  return payload;
}

let csrfToken = "";
function readCookie(name) {
  return document.cookie.split("; ").find((cookie) => cookie.startsWith(`${name}=`))?.split("=").slice(1).join("=") || "";
}

function showToast(message, isError = false) {
  const toast = $("#toast");
  toast.textContent = message;
  toast.classList.toggle("error", isError);
  toast.classList.add("show");
  clearTimeout(state.toastTimer);
  state.toastTimer = setTimeout(() => toast.classList.remove("show"), 3200);
}

async function loadCsrf() {
  try {
    const data = await api("/auth/csrf");
    csrfToken = data.token || readCookie("XSRF-TOKEN");
  } catch (error) {
    showToast(`Can't connect to the bookstore API: ${error.message}`, true);
  }
}

function renderBooks() {
  const books = state.books.filter((book) => {
    const search = state.query.trim().toLowerCase();
    const matchesText = !search || `${book.title} ${book.author} ${book.category?.name || ""}`.toLowerCase().includes(search);
    const matchesCategory = !state.categoryId || String(book.category?.id) === state.categoryId;
    return matchesText && matchesCategory;
  });
  const grid = $("#book-grid");
  grid.innerHTML = books.map((book, index) => {
    const colors = ["#5d735e", "#a85c43", "#526277", "#b28a5e", "#58675e", "#835f69"];
    const fallback = `<div class="book-art-fallback ${book.imageUrl ? "hidden" : ""}" style="background:${colors[index % colors.length]}"><span>${escapeHtml(book.title)}</span><small>${escapeHtml(book.author)}</small></div>`;
    return `<article class="book-card">
      <div class="book-art" data-details="${book.id}">${book.stock <= 0 ? '<span class="book-badge">Sold out</span>' : book.rating >= 4.7 ? '<span class="book-badge">Reader favorite</span>' : ""}${book.imageUrl ? `<img src="${escapeHtml(book.imageUrl)}" alt="Cover of ${escapeHtml(book.title)}" loading="lazy" onerror="this.remove();this.parentElement.querySelector('.book-art-fallback').classList.remove('hidden')">` : ""}${fallback}</div>
      <div class="book-meta"><span>${escapeHtml(book.category?.name || "Books")}</span><span class="book-rating">★ ${Number(book.rating || 0).toFixed(1)}</span></div>
      <h3 class="book-title" data-details="${book.id}">${escapeHtml(book.title)}</h3>
      <p class="book-author">by ${escapeHtml(book.author)}</p>
      <div class="book-buy-row"><span class="book-price">${money(book.price)}</span><button class="add-button" data-add="${book.id}" aria-label="Add ${escapeHtml(book.title)} to bag" ${book.stock <= 0 ? "disabled" : ""}>+</button></div>
    </article>`;
  }).join("");
  $("#empty-state").classList.toggle("hidden", books.length > 0);
  if (state.user?.role === "ADMIN") renderAdminBooks();
}

async function loadBooks() {
  const params = new URLSearchParams();
  if (state.query) params.set("q", state.query);
  if (state.categoryId) params.set("categoryId", state.categoryId);
  state.books = await api(`/books${params.size ? `?${params}` : ""}`);
  renderBooks();
}

async function loadCategories() {
  state.categories = await api("/categories");
  $("#category-filter").innerHTML = '<option value="">All categories</option>' + state.categories.map((category) => `<option value="${category.id}">${escapeHtml(category.name)}</option>`).join("");
  $("#categories").innerHTML = `<button class="category-pill ${state.categoryId ? "" : "active"}" data-category="">Everything</button>` + state.categories.map((category) => `<button class="category-pill ${state.categoryId === String(category.id) ? "active" : ""}" data-category="${category.id}">${escapeHtml(category.name)}</button>`).join("");
  if (state.user?.role === "ADMIN") {
    $("#admin-category").innerHTML = state.categories.map((category) => `<option value="${category.id}">${escapeHtml(category.name)}</option>`).join("");
  }
}

async function loadCart() {
  if (!state.user) {
    state.cart = null;
    renderCart();
    return;
  }
  state.cart = await api("/cart");
  renderCart();
}

function renderCart() {
  const items = state.cart?.items || [];
  const count = items.reduce((total, item) => total + item.quantity, 0);
  $("#cart-count").textContent = count;
  $("#drawer-count").textContent = `(${count})`;
  $("#cart-total").textContent = money(state.cart?.total);
  $("#cart-items").innerHTML = items.length ? items.map((item) => `<article class="cart-line">
    ${item.imageUrl ? `<img class="cart-thumb" src="${escapeHtml(item.imageUrl)}" alt="" onerror="this.style.visibility='hidden'">` : '<div class="cart-thumb"></div>'}
    <div><h3>${escapeHtml(item.title)}</h3><p>${money(item.unitPrice)}</p><div class="quantity-control"><button data-quantity="${item.bookId}" data-value="${item.quantity - 1}" aria-label="Decrease quantity">−</button><span>${item.quantity}</span><button data-quantity="${item.bookId}" data-value="${item.quantity + 1}" aria-label="Increase quantity">+</button></div></div>
    <div class="line-price">${money(item.unitPrice * item.quantity)}<button class="remove-item" data-remove="${item.bookId}">Remove</button></div>
  </article>`).join("") : '<p class="cart-empty">Your bag is waiting for a good story.</p>';
}

function updateAccountUI() {
  const accountButton = $("#account-button");
  accountButton.textContent = state.user ? `Hi, ${state.user.name.split(" ")[0]}` : "Log in";
  $("#order-history-button").classList.toggle("hidden", !state.user);
  $("#logout-button")?.classList.toggle("hidden", !state.user);
  $("#auth-form").classList.toggle("hidden", Boolean(state.user));
  $("#switch-auth").classList.toggle("hidden", Boolean(state.user));
  $("#admin-panel").classList.toggle("hidden", state.user?.role !== "ADMIN");
  if (state.user?.role === "ADMIN") {
    loadCategories().catch((error) => showToast(error.message, true));
    loadAdminOrders().catch((error) => showToast(error.message, true));
    renderAdminBooks();
  }
}

function openDialog(dialog) {
  if (!dialog.open) dialog.showModal();
}

function openCart() {
  $("#overlay").classList.remove("hidden");
  $("#cart-drawer").classList.add("open");
  $("#cart-drawer").setAttribute("aria-hidden", "false");
}

function closeCart() {
  $("#overlay").classList.add("hidden");
  $("#cart-drawer").classList.remove("open");
  $("#cart-drawer").setAttribute("aria-hidden", "true");
}

function setAuthMode(mode) {
  state.authMode = mode;
  const registering = mode === "register";
  $("#name-field").classList.toggle("hidden", !registering);
  $("#auth-name").required = registering;
  $("#auth-password").autocomplete = registering ? "new-password" : "current-password";
  $("#auth-title").innerHTML = registering ? "Join the reading room<span class=\"accent-dot\">.</span>" : "Welcome back<span class=\"accent-dot\">.</span>";
  $("#auth-subtitle").textContent = registering ? "Create an account to keep your bookish things together." : "Log in to keep your bookish things together.";
  $("#auth-submit").innerHTML = `${registering ? "Create my account" : "Log in"} <span>→</span>`;
  $("#switch-auth").innerHTML = registering ? 'Already a member? <button type="button" id="auth-mode-toggle">Log in</button>' : 'New around here? <button type="button" id="auth-mode-toggle">Create an account</button>';
}

async function showBookDetails(id) {
  try {
    const book = await api(`/books/${id}`);
    $("#book-details").innerHTML = `<div class="details-content">
      ${book.imageUrl ? `<img class="details-cover" src="${escapeHtml(book.imageUrl)}" alt="Cover of ${escapeHtml(book.title)}" onerror="this.style.visibility='hidden'">` : `<div class="details-cover book-art-fallback"><span>${escapeHtml(book.title)}</span><small>${escapeHtml(book.author)}</small></div>`}
      <div class="details-info"><p class="eyebrow">${escapeHtml(book.category?.name || "Book")} · ★ ${Number(book.rating || 0).toFixed(1)}</p><h2>${escapeHtml(book.title)}</h2><p class="details-author">by ${escapeHtml(book.author)}</p><p class="details-description">${escapeHtml(book.description || "A story waiting to be discovered.")}</p><p class="details-price">${money(book.price)}</p><p class="details-author">${book.stock > 0 ? `${book.stock} in stock` : "Currently sold out"}</p><button class="primary-button" data-add="${book.id}" ${book.stock <= 0 ? "disabled" : ""}>Add to bag <span>→</span></button></div>
    </div>`;
    openDialog($("#details-dialog"));
  } catch (error) { showToast(error.message, true); }
}

async function addToCart(bookId) {
  if (!state.user) {
    showToast("Log in or create an account to add books to your bag.");
    setAuthMode("login");
    openDialog($("#account-dialog"));
    return;
  }
  try {
    await api("/cart/items", { method: "POST", body: JSON.stringify({ bookId: Number(bookId), quantity: 1 }) });
    await loadCart();
    showToast("Added to your bag.");
  } catch (error) { showToast(error.message, true); }
}

async function submitAuth(event) {
  event.preventDefault();
  const registering = state.authMode === "register";
  const body = registering
    ? { name: $("#auth-name").value.trim(), email: $("#auth-email").value.trim(), password: $("#auth-password").value }
    : { email: $("#auth-email").value.trim(), password: $("#auth-password").value };
  try {
    if (registering) await api("/auth/register", { method: "POST", body: JSON.stringify(body) });
    await api("/auth/login", { method: "POST", body: JSON.stringify({ email: body.email, password: body.password }) });
    await loadCsrf();
    state.user = await api("/auth/me");
    $("#account-dialog").close();
    $("#auth-form").reset();
    updateAccountUI();
    await loadCart();
    showToast(registering ? "Welcome to Chapter & Co.!" : "You're back in the reading room.");
  } catch (error) { showToast(error.message, true); }
}

async function logout() {
  try {
    await api("/auth/logout", { method: "POST" });
    state.user = null;
    state.cart = null;
    updateAccountUI();
    renderCart();
    $("#account-dialog").close();
    showToast("You have been logged out.");
  } catch (error) { showToast(error.message, true); }
}

async function changeQuantity(bookId, quantity) {
  try {
    if (quantity < 1) await api(`/cart/items/${bookId}`, { method: "DELETE" });
    else await api(`/cart/items/${bookId}`, { method: "PUT", body: JSON.stringify({ quantity }) });
    await loadCart();
  } catch (error) { showToast(error.message, true); }
}

async function loadOrders() {
  const orders = await api("/orders");
  $("#order-list").innerHTML = orders.length ? orders.map((order) => `<article class="order-card"><div class="order-card-top"><strong>Order #${order.id}</strong><span>${escapeHtml(order.status)}</span></div><p>${new Date(order.createdAt).toLocaleDateString()} · ${order.items.reduce((sum, item) => sum + item.quantity, 0)} books · <strong>${money(order.total)}</strong></p><p>${order.items.map((item) => `${escapeHtml(item.title)} × ${item.quantity}`).join(", ")}</p></article>`).join("") : '<p class="cart-empty">No orders just yet. Your next favorite is waiting.</p>';
  openDialog($("#orders-dialog"));
}

async function createOrder(event) {
  event.preventDefault();
  const form = new FormData(event.currentTarget);
  const body = Object.fromEntries(form.entries());
  try {
    const order = await api("/orders", { method: "POST", body: JSON.stringify(body) });
    $("#checkout-dialog").close();
    closeCart();
    event.currentTarget.reset();
    await loadCart();
    showToast(`Order #${order.id} placed — happy reading!`);
  } catch (error) { showToast(error.message, true); }
}

function renderAdminBooks() {
  const target = $("#admin-book-list");
  if (!target) return;
  target.innerHTML = state.books.map((book) => `<div class="admin-row"><span>${escapeHtml(book.title)}<small>${escapeHtml(book.author)} · Stock ${book.stock}</small></span><span><button class="admin-edit" data-edit-book="${book.id}">Edit</button><button class="admin-delete" data-delete-book="${book.id}">Delete</button></span></div>`).join("");
}

async function loadAdminOrders() {
  const orders = await api("/admin/orders");
  $("#admin-order-list").innerHTML = orders.length ? orders.map((order) => `<div class="admin-row"><span>Order #${order.id}<small>${escapeHtml(order.userName)} · ${money(order.total)}</small></span><select data-order-status="${order.id}">${["PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED"].map((status) => `<option ${status === order.status ? "selected" : ""}>${status}</option>`).join("")}</select></div>`).join("") : '<p class="cart-empty">No customer orders yet.</p>';
}

async function saveAdminBook(event) {
  event.preventDefault();
  const form = event.currentTarget;
  const data = Object.fromEntries(new FormData(form).entries());
  data.price = Number(data.price);
  data.rating = Number(data.rating || 0);
  data.stock = Number(data.stock);
  data.categoryId = Number(data.categoryId);
  const id = $("#admin-book-id").value;
  try {
    await api(id ? `/admin/books/${id}` : "/admin/books", { method: id ? "PUT" : "POST", body: JSON.stringify(data) });
    form.reset();
    $("#admin-book-id").value = "";
    $("#admin-save").textContent = "Add book";
    await loadBooks();
    showToast(id ? "Book updated." : "Book added to the collection.");
  } catch (error) { showToast(error.message, true); }
}

async function handleAdminClick(event) {
  const deleteButton = event.target.closest("[data-delete-book]");
  const editButton = event.target.closest("[data-edit-book]");
  if (deleteButton) {
    if (!confirm("Remove this book from the catalog?")) return;
    try {
      await api(`/admin/books/${deleteButton.dataset.deleteBook}`, { method: "DELETE" });
      await loadBooks();
      showToast("Book removed.");
    } catch (error) { showToast(error.message, true); }
  }
  if (editButton) {
    const book = state.books.find((item) => String(item.id) === editButton.dataset.editBook);
    if (!book) return;
    $("#admin-book-id").value = book.id;
    for (const [name, value] of Object.entries({ title: book.title, author: book.author, description: book.description, isbn: book.isbn, imageUrl: book.imageUrl, price: book.price, rating: book.rating, stock: book.stock, categoryId: book.category?.id })) {
      const field = $(`[name="${name}"]`, $("#admin-book-form"));
      if (field) field.value = value ?? "";
    }
    $("#admin-save").textContent = "Save changes";
    $("#admin-book-form").scrollIntoView({ behavior: "smooth", block: "center" });
  }
}

async function initialize() {
  $("#year").textContent = new Date().getFullYear();
  try {
    await loadCsrf();
    [state.categories] = await Promise.all([api("/categories")]);
    await loadBooks();
    $("#category-filter").innerHTML = '<option value="">All categories</option>' + state.categories.map((category) => `<option value="${category.id}">${escapeHtml(category.name)}</option>`).join("");
    $("#categories").innerHTML = `<button class="category-pill active" data-category="">Everything</button>` + state.categories.map((category) => `<button class="category-pill" data-category="${category.id}">${escapeHtml(category.name)}</button>`).join("");
    try { state.user = await api("/auth/me"); } catch (error) { if (error.status === 401) state.user = null; else throw error; }
    updateAccountUI();
    await loadCart();
  } catch (error) {
    $("#book-grid").innerHTML = '<p class="loading-message">We couldn’t reach the bookstore. Please make sure the Spring Boot API and MySQL are running, then refresh.</p>';
    showToast(error.message, true);
  }
}

$("#search-form").addEventListener("submit", async (event) => {
  event.preventDefault();
  state.query = $("#search-input").value.trim();
  state.categoryId = $("#category-filter").value;
  $$(".category-pill").forEach((pill) => pill.classList.toggle("active", pill.dataset.category === state.categoryId));
  try { await loadBooks(); } catch (error) { showToast(error.message, true); }
});
$("#categories").addEventListener("click", async (event) => {
  const pill = event.target.closest("[data-category]");
  if (!pill) return;
  state.categoryId = pill.dataset.category;
  $("#category-filter").value = state.categoryId;
  $$(".category-pill").forEach((item) => item.classList.toggle("active", item === pill));
  try { await loadBooks(); } catch (error) { showToast(error.message, true); }
});
$("#book-grid").addEventListener("click", (event) => {
  const add = event.target.closest("[data-add]");
  if (add) { addToCart(add.dataset.add); return; }
  const details = event.target.closest("[data-details]");
  if (details) showBookDetails(details.dataset.details);
});
$("#book-details").addEventListener("click", (event) => {
  const add = event.target.closest("[data-add]");
  if (add) addToCart(add.dataset.add);
});
$("#cart-items").addEventListener("click", (event) => {
  const remove = event.target.closest("[data-remove]");
  const quantity = event.target.closest("[data-quantity]");
  if (remove) changeQuantity(remove.dataset.remove, 0);
  if (quantity) changeQuantity(quantity.dataset.quantity, Number(quantity.dataset.value));
});
$("#cart-button").addEventListener("click", async () => {
  try { await loadCart(); openCart(); } catch (error) { showToast(error.message, true); }
});
$("#overlay").addEventListener("click", closeCart);
$$("[data-close-drawer]").forEach((button) => button.addEventListener("click", closeCart));
$("#checkout-button").addEventListener("click", () => {
  if (!state.user) { closeCart(); setAuthMode("login"); openDialog($("#account-dialog")); return; }
  if (!state.cart?.items?.length) { showToast("Your bag is empty."); return; }
  openDialog($("#checkout-dialog"));
});
$("#checkout-form").addEventListener("submit", createOrder);
$("#account-button").addEventListener("click", () => { setAuthMode("login"); openDialog($("#account-dialog")); });
$("#switch-auth").addEventListener("click", (event) => {
  if (event.target.closest("#auth-mode-toggle")) setAuthMode(state.authMode === "login" ? "register" : "login");
});
$("#auth-form").addEventListener("submit", submitAuth);
$("#logout-button")?.addEventListener("click", logout);
$("#order-history-button").addEventListener("click", async () => {
  try { await loadOrders(); } catch (error) { showToast(error.message, true); }
});
$$("[data-close-dialog]").forEach((button) => button.addEventListener("click", () => button.closest("dialog").close()));
$("#search-toggle").addEventListener("click", () => $("#search-input").focus());
$("#menu-toggle").addEventListener("click", () => $(".main-nav").classList.toggle("open"));
$$("[data-view]").forEach((link) => link.addEventListener("click", () => {
  $$(".nav-link").forEach((item) => item.classList.toggle("active", item === link));
  $(".main-nav").classList.remove("open");
}));
$("#newsletter-form").addEventListener("submit", async (event) => {
  event.preventDefault();
  try {
    await api("/newsletter", { method: "POST", body: JSON.stringify({ email: $("#newsletter-email").value.trim() }) });
    event.currentTarget.reset();
    showToast("You're on the list. Look out for a little note from us.");
  } catch (error) { showToast(error.message, true); }
});
$("#admin-book-form")?.addEventListener("submit", saveAdminBook);
$("#admin-book-list")?.addEventListener("click", handleAdminClick);
$("#admin-order-list")?.addEventListener("change", async (event) => {
  const select = event.target.closest("[data-order-status]");
  if (!select) return;
  try {
    await api(`/admin/orders/${select.dataset.orderStatus}/status`, { method: "PATCH", body: JSON.stringify({ status: select.value }) });
    showToast("Order status updated.");
  } catch (error) { showToast(error.message, true); }
});

document.addEventListener("keydown", (event) => { if (event.key === "Escape") closeCart(); });
initialize();
