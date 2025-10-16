// esbuild-plugins.ts
import { copy } from 'esbuild-plugin-copy';
import { Plugin } from 'esbuild';

const esbuildPlugins: Plugin[] = [
  copy({
    verbose: true,
    assets: [
      {
        from: ['./node_modules/dggal/dist/libdggal_c_fn.js.0.0.wasm'], // Source path(s) to copy
        to: './src/assets/wasm/libdggal_c_fn.js.0.0.wasm', // Destination path in the output directory
      }
    ],
  }),
  // Add other esbuild plugins here if needed
];

export default esbuildPlugins;