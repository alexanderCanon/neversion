import { type RouteConfig, index, layout, route } from '@react-router/dev/routes'

export default [
  layout('routes/_layout.tsx', [
    index('routes/home.tsx'),
    route('platforms', 'routes/platforms.tsx'),
    route('platforms/:platformId', 'routes/platform-detail.tsx'),
    route('games', 'routes/games.tsx'),
    route('games/:slug', 'routes/game-detail.tsx'),
    route('combo', 'routes/combo.tsx'),
    route('offers', 'routes/offers.tsx'),
    route('login', 'routes/login.tsx'),
    route('checkout', 'routes/checkout.tsx'),
    route('payment', 'routes/payment.tsx'),
    route('payment-page', 'routes/payment.tsx', { id: 'payment-page' }),
    route('customer-panel', 'routes/customer-panel.tsx'),
    route('how-to-buy', 'routes/how-to-buy.tsx'),
    route('payment-methods', 'routes/payment-methods.tsx'),
    route('wholesalers', 'routes/wholesalers.tsx'),
    route('contact', 'routes/contact.tsx'),
    route('support', 'routes/support.tsx'),
    route('*', 'routes/catch-all.tsx'),
  ]),
] satisfies RouteConfig
