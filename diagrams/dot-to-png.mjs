import { Graphviz } from "@hpcc-js/wasm-graphviz";
import { readFileSync, writeFileSync } from "fs";
import sharp from "sharp";
import { fileURLToPath } from "url";
import { dirname, join } from "path";

const __dirname = dirname(fileURLToPath(import.meta.url));
const dotFile  = join(__dirname, "indigoengine.dot");
const pngFile  = join(__dirname, "indigoengine.png");

const dotSource = readFileSync(dotFile, "utf8");

const graphviz = await Graphviz.load();
const svg = graphviz.dot(dotSource, "svg");

await sharp(Buffer.from(svg)).png().toFile(pngFile);

console.log("Diagram written to diagrams/indigoengine.png");
