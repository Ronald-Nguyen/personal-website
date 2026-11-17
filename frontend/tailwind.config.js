/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui', '-apple-system', 'Segoe UI', 'Roboto', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'ui-monospace', 'SFMono-Regular', 'Menlo', 'monospace'],
      },
      colors: {
        accent: {
          50: '#eef6ff',
          100: '#d9eaff',
          200: '#bcdaff',
          300: '#8ec3ff',
          400: '#59a1ff',
          500: '#337dfc',
          600: '#1d5ef1',
          700: '#1649de',
          800: '#183db4',
          900: '#1a378e',
        },
      },
      keyframes: {
        blink: { '0%, 80%, 100%': { opacity: '0.2' }, '40%': { opacity: '1' } },
      },
      animation: {
        blink: 'blink 1.4s infinite both',
      },
    },
  },
  plugins: [],
};
