# neversion-store

React + Vite + Tailwind CSS project.

## Development Server

- `pnpm dev` to start the Vite development server.
- Preview URL: Default http://localhost:5173 (or $PORT).
- Hot reload: Changes to source files are reflected immediately.

## Project Structure

- `src/main.tsx` - React entrypoint; imports `src/index.css` and mounts `src/App.tsx` into the `#root` element
- `src/App.tsx` - Primary application component and main UI
- `src/index.css` - Global CSS entrypoint and Tailwind CSS v4 import
- `index.html` - Vite HTML shell loading `src/main.tsx`
- `package.json` - Project dependencies and scripts
- `vite.config.ts` - Vite configuration with React and Tailwind CSS v4 plus the `@` alias for `src`
- `.mise.toml` - Toolchain versions for Node.js 24 and pnpm 11

## Dependencies

- Runtime: React 19 and React DOM 19
- Styling: Tailwind CSS v4 with the `@tailwindcss/vite` plugin
- Build tooling: Vite 8, TypeScript 7.x, and `@vitejs/plugin-react`
- Toolchain: Node.js 24, pnpm 11
- Formatting: oxfmt

## Styling

This project uses **Tailwind CSS v4** through the `@tailwindcss/vite` plugin configured in `vite.config.ts`. `src/index.css` imports Tailwind with `@import 'tailwindcss';`. Use Tailwind utility classes directly in JSX and put global CSS or Tailwind v4 theme customization in `src/index.css`.

## Code quality

- Use double quotes for strings containing apostrophes (`"We're here to help"`), or escape them in single-quoted strings.
- Ensure JSX tags are closed and braces are balanced.
- Export components cleanly.
