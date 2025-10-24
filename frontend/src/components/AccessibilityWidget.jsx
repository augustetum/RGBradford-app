import React, { useEffect, useState } from "react";

const Size = {
  Md: 0,
  Lg: 4,
  Xl: 8,
};
const x_icon = `https://static.igem.wiki/teams/5780/accesibillity/x-icon.svg`;
const text_size_icon = `https://static.igem.wiki/teams/5780/accesibillity/text-size-icon.svg`;
const line_height_icon = `https://static.igem.wiki/teams/5780/accesibillity/line-height-icon.svg`;
const letter_spacing_icon = `https://static.igem.wiki/teams/5780/accesibillity/letter-spacing-icon.svg`;
const font_icon = `https://static.igem.wiki/teams/5780/accesibillity/font-icon.svg`;
const contrast_icon = `https://static.igem.wiki/teams/5780/accesibillity/contrast-icon.svg`;
const reset_icon = `https://static.igem.wiki/teams/5780/accesibillity/reset-icon.svg`;
const accessibility_icon = `https://static.igem.wiki/teams/5780/accesibillity/accessibility-icon.webp`;

export function AccessibilityWidget({
  hideAccessibilityWidget,
}) {
  const textClasses = [
    "text-xs",
    "text-sm",
    "text-base",
    "text-md",
    "text-lg",
    "text-xl",
    "text-2xl",
    "text-3xl",
    "text-4xl",
    "text-5xl",
    "text-6xl",
    "text-7xl",
    "text-8xl",
    "text-9xl",
  ];

  const [open, setOpen] = useState(false);
  const [textSize, setTextSize] = useState(Size.Md);
  const [lineHeight, setLineHeight] = useState(Size.Md);
  const [letterSpacing, setLetterSpacing] = useState(Size.Md);
  const [fontFamily, setFontFamily] = useState("Inter");
  const [contrastMode, setContrastMode] = useState("normal");

  const openDrawer = () => setOpen(true);
  const closeDrawer = () => setOpen(false);

  const applyFontFamily = (font) => {
    setFontFamily(font);

    textClasses.forEach((textClass) => {
      const elements = document.querySelectorAll(
        `.${textClass}:not(.ignore-accessibility)`,
      );
      elements.forEach((el) => {
        el.style.fontFamily = font;
      });
    });
    typeof window !== "undefined" && localStorage.setItem("fontFamily", font);
  };

  const applyContrast = (mode) => {
    setContrastMode(mode);

    // Remove inverted-mode class if it exists
    document.documentElement.classList.remove("inverted-mode");

    if (mode === "high-contrast") {
      document.documentElement.style.setProperty("--color-igem-black", "oklch(0.872 0.01 258.338)");
      document.documentElement.style.setProperty("--color-igem-white", "oklch(0.21 0.034 264.665)");
      document.documentElement.style.setProperty("--color-igem-dblue", "#669bbc");
      document.documentElement.style.setProperty("--color-igem-dblue-highlight", "#004366");
      document.documentElement.style.setProperty("--color-igem-blue", "#003049");
      document.documentElement.style.setProperty("--color-igem-dred", "#c12126");
      document.documentElement.style.setProperty("--color-igem-red", "#761113");
    }

    if (mode === "inverted" || mode === "normal") {
      document.documentElement.style.setProperty("--color-igem-black", "#171717");
      document.documentElement.style.setProperty("--color-igem-white", "#ffffff");
      document.documentElement.style.setProperty("--color-igem-dblue", "#003049");
      document.documentElement.style.setProperty("--color-igem-dblue-highlight", "#004366");
      document.documentElement.style.setProperty("--color-igem-blue", "#669bbc");
      document.documentElement.style.setProperty("--color-igem-dred", "#761113");
      document.documentElement.style.setProperty("--color-igem-red", "#c12126");
    }

    if (mode === "inverted") {
      document.documentElement.classList.add("inverted-mode");
    }

    const event = new CustomEvent("contrastModeChange", { detail: mode });
    window.dispatchEvent(event);

    typeof window !== "undefined" && localStorage.setItem("contrastMode", mode);
  };

  const applyTextSize = (size) => {
    setTextSize(size);
    // Calculate multiplier based on size
    // Size.Md = 0, Size.Lg = 1, Size.Xl = 2
    // Multiplier: Md = 1.0, Lg = 1.25, Xl = 1.5
    const multiplier = 1 + (size - Size.Md) * 0.125;

    // Set CSS custom property on document root
    document.documentElement.style.setProperty('--accessibility-font-multiplier', multiplier);

    typeof window !== "undefined" &&
      localStorage.setItem("textSize", size);
  };

  const applyLineHeight = (size) => {
    setLineHeight(size);

    // Calculate offset based on size
    // Size.Md = 0, Size.Lg = 1, Size.Xl = 2
    // Offset: Md = 0px, Lg = 2px, Xl = 4px
    const offset = (size - Size.Md) * 2;

    // Set CSS custom property on document root
    document.documentElement.style.setProperty('--accessibility-line-height-offset', `${offset}px`);

    typeof window !== "undefined" &&
      localStorage.setItem("lineHeight", size);
  };

  const applyLetterSpacing = (size) => {
    setLetterSpacing(size);

    // Calculate offset based on size
    // Size.Md = 0, Size.Lg = 1, Size.Xl = 2
    // Offset: Md = 0px, Lg = 0.5px, Xl = 1px
    const offset = (size - Size.Md) * 0.5;

    // Set CSS custom property on document root
    document.documentElement.style.setProperty('--accessibility-letter-spacing-offset', `${offset}px`);

    typeof window !== "undefined" &&
      localStorage.setItem("letterSpacing", size);
  };

  useEffect(() => {
    setTimeout(() => {
      applyTextSize(getSavedProperty("textSize"));
      applyLineHeight(getSavedProperty("lineHeight"));
      applyLetterSpacing(getSavedProperty("letterSpacing"));

      applyContrast(localStorage.getItem("contrastMode") || "normal");

      if (typeof window !== "undefined") {
        applyFontFamily(
          localStorage.getItem("fontFamily") || "Inter",
        );
      }
    }, 50);

    // Set up MutationObserver to watch for new elements and apply font family
    const observer = new MutationObserver((mutations) => {
      const savedFontFamily = typeof window !== "undefined" ? localStorage.getItem("fontFamily") : null;

      if (savedFontFamily && savedFontFamily !== "Inter") {
        mutations.forEach((mutation) => {
          mutation.addedNodes.forEach((node) => {
            if (node.nodeType === 1) { // Element node
              // Apply font family to the node itself
              textClasses.forEach((textClass) => {
                if (node.classList && node.classList.contains(textClass) && !node.classList.contains('ignore-accessibility')) {
                  node.style.fontFamily = savedFontFamily;
                }
              });

              // Apply font family to descendants
              textClasses.forEach((textClass) => {
                const elements = node.querySelectorAll?.(`.${textClass}:not(.ignore-accessibility)`);
                elements?.forEach((el) => {
                  el.style.fontFamily = savedFontFamily;
                });
              });
            }
          });
        });
      }
    });

    // Start observing the document body for changes
    observer.observe(document.body, {
      childList: true,
      subtree: true,
    });

    // Cleanup function
    return () => {
      observer.disconnect();
    };
  }, []);

  const resetSettings = () => {
    localStorage.removeItem("textSize");
    localStorage.removeItem("lineHeight");
    localStorage.removeItem("letterSpacing");
    localStorage.removeItem("fontFamily");
    localStorage.removeItem("contrastMode");
    window.location.reload();
  };

  const getSavedProperty = (propertyName) => {
    if (typeof window !== "undefined") {
      const savedTextSize = localStorage.getItem(propertyName);

      if (savedTextSize) {
        return savedTextSize;
        // const sizeKey = Object.keys(Size).find((key) => key === savedTextSize);
        // if (sizeKey) {
        //   return Size[sizeKey];
        // }
      }
    }
    return Size.Md;
  };

  return (
    <>
      {/* Accessibility Widget Toggle Button */}
      <button
        onClick={openDrawer}
        className={
          hideAccessibilityWidget
            ? "hidden"
              : "bg-transparent hover:bg-transparent fixed bottom-10 left-2 p-3 hover:scale-105 z-[60] w-20 h-20"
        }
        aria-label="Open Accessibility Options"
      >
        <img src={accessibility_icon} alt="Accessibility Options" />
      </button>

      {/* Drawer Overlay */}
      {open && (
        <div
          className={`${open ? "" : "hidden"} fixed inset-0 bg-transparent bg-opacity-50 z-[70]`}
          onClick={closeDrawer}
        />
      )}

      {/* Drawer */}
      <div
        className={`${open ? "" : "hidden"} fixed top-0 left-0 h-full w-80 bg-gray-300 transform transition-transform duration-300 ease-in-out z-[80] overflow-y-auto ${
          open ? "translate-x-0" : "-translate-x-full"
        }`}
      >
        {/* Header */}
        <div className="flex items-center justify-between p-4 bg-blue-600 mb-2">
          <h1 className="text-white font-bold text-xl ignore-accessibility">
            Accessibility Options
          </h1>
          <button
            onClick={closeDrawer}
            className="p-2 text-white hover:bg-blue-700 rounded-md transition-colors"
          >
            <img src={x_icon} alt="Close" className="w-6 h-6" />
          </button>
        </div>

        {/* Text Size Card */}
        <div className="m-3 bg-white rounded-lg shadow-md">
          <div className="pr-3 pl-3 pt-3 pb-1 text-gray-900 ml-0.5 text-lg font-medium ignore-accessibility">
            <div className="flex justify-center items-center">
              <img src={text_size_icon} alt="" className="w-10 h-10" />
            </div>
            <h2>Text size</h2>
          </div>
          <div className="pr-3 pl-3 pb-3">
            <div className="flex bg-gray-100 rounded-lg p-1">
              <button
                className={`flex-1 px-3 py-2 text-sm font-medium rounded-md transition-colors ignore-accessibility ${
                  getSavedProperty("textSize") == Size.Md
                    ? "bg-white text-gray-900 shadow-sm"
                    : "text-gray-700 hover:text-gray-900"
                }`}
                onClick={() => (applyTextSize(Size.Md))}
              >
                Small
              </button>
              <button
                className={`flex-1 px-3 py-2 text-sm font-medium rounded-md transition-colors ignore-accessibility ${
                  getSavedProperty("textSize") == Size.Lg
                    ? "bg-white text-gray-900 shadow-sm"
                    : "text-gray-700 hover:text-gray-900"
                }`}
                onClick={() => applyTextSize(Size.Lg)}
              >
                Medium
              </button>
              <button
                className={`flex-1 px-3 py-2 text-sm font-medium rounded-md transition-colors ignore-accessibility ${
                  getSavedProperty("textSize") == Size.Xl
                    ? "bg-white text-gray-900 shadow-sm"
                    : "text-gray-700 hover:text-gray-900"
                }`}
                onClick={() => applyTextSize(Size.Xl)}
              >
                Large
              </button>
            </div>
          </div>
        </div>

        {/* Line Height Card */}
        <div className="m-3 bg-white rounded-lg shadow-md">
          <div className="pr-3 pl-3 pt-3 pb-1 text-gray-900 ml-0.5 text-lg font-medium ignore-accessibility">
            <div className="flex justify-center items-center">
              <img src={line_height_icon} alt="" className="w-10 h-10" />
            </div>
            <h2>Line height</h2>
          </div>
          <div className="pr-3 pl-3 pb-3">
            <div className="flex bg-gray-100 rounded-lg p-1">
              <button
                className={`flex-1 px-3 py-2 text-sm font-medium rounded-md transition-colors ignore-accessibility ${
                  getSavedProperty("lineHeight") == Size.Md
                    ? "bg-white text-gray-900 shadow-sm"
                    : "text-gray-700 hover:text-gray-900"
                }`}
                onClick={() => applyLineHeight(Size.Md)}
              >
                Small
              </button>
              <button
                className={`flex-1 px-3 py-2 text-sm font-medium rounded-md transition-colors ignore-accessibility ${
                  getSavedProperty("lineHeight") == Size.Lg
                    ? "bg-white text-gray-900 shadow-sm"
                    : "text-gray-700 hover:text-gray-900"
                }`}
                onClick={() => applyLineHeight(Size.Lg)}
              >
                Medium
              </button>
              <button
                className={`flex-1 px-3 py-2 text-sm font-medium rounded-md transition-colors ignore-accessibility ${
                  getSavedProperty("lineHeight") == Size.Xl
                    ? "bg-white text-gray-900 shadow-sm"
                    : "text-gray-700 hover:text-gray-900"
                }`}
                onClick={() => applyLineHeight(Size.Xl)}
              >
                Large
              </button>
            </div>
          </div>
        </div>

        {/* Letter Spacing Card */}
        <div className="m-3 bg-white rounded-lg shadow-md">
          <div className="pr-3 pl-3 pt-3 pb-1 text-gray-900 ml-0.5 text-lg font-medium ignore-accessibility">
            <div className="flex justify-center items-center">
              <img src={letter_spacing_icon} alt="" className="w-10 h-10" />
            </div>
            <h2>Letter spacing</h2>
          </div>
          <div className="pr-3 pl-3 pb-3">
            <div className="flex bg-gray-100 rounded-lg p-1">
              <button
                className={`flex-1 px-3 py-2 text-sm font-medium rounded-md transition-colors ignore-accessibility ${
                  getSavedProperty("letterSpacing") == Size.Md
                    ? "bg-white text-gray-900 shadow-sm"
                    : "text-gray-700 hover:text-gray-900"
                }`}
                onClick={() => applyLetterSpacing(Size.Md)}
              >
                Small
              </button>
              <button
                className={`flex-1 px-3 py-2 text-sm font-medium rounded-md transition-colors ignore-accessibility ${
                  getSavedProperty("letterSpacing") == Size.Lg
                    ? "bg-white text-gray-900 shadow-sm"
                    : "text-gray-700 hover:text-gray-900"
                }`}
                onClick={() => applyLetterSpacing(Size.Lg)}
              >
                Medium
              </button>
              <button
                className={`flex-1 px-3 py-2 text-sm font-medium rounded-md transition-colors ignore-accessibility ${
                  getSavedProperty("letterSpacing") == Size.Xl
                    ? "bg-white text-gray-900 shadow-sm"
                    : "text-gray-700 hover:text-gray-900"
                }`}
                onClick={() => applyLetterSpacing(Size.Xl)}
              >
                Large
              </button>
            </div>
          </div>
        </div>

        {/* Font Card */}
        <div className="m-3 bg-white rounded-lg shadow-md">
          <div className="pr-3 pl-3 pt-3 pb-1 text-gray-900 text-lg font-medium ignore-accessibility">
            <div className="flex justify-center items-center">
              <img src={font_icon} alt="" className="w-10 h-10" />
            </div>
            <h2>Font</h2>
          </div>
          <div className="pr-3 pl-3 pb-3">
            <div className="flex bg-gray-100 rounded-lg p-1">
              <button
                className={` ${open ? "" : "hidden"} flex-1 px-3 py-2 text-sm font-medium rounded-md transition-colors ignore-accessibility ${
                  (typeof window !== "undefined" && localStorage.getItem("fontFamily")) === "Inter"
                    ? "bg-white text-gray-900 shadow-sm"
                    : "text-gray-700 hover:text-gray-900"
                }`}
                onClick={() => applyFontFamily("Inter")}
              >
                Default
              </button>
              <button
                className={`flex-1 px-3 py-2 text-sm font-medium rounded-md transition-colors ignore-accessibility ${
                  (typeof window !== "undefined" && localStorage.getItem("fontFamily")) === "OpenDyslexic"
                    ? "bg-white text-gray-900 shadow-sm"
                    : "text-gray-700 hover:text-gray-900"
                }`}
                onClick={() => applyFontFamily("OpenDyslexic")}
              >
                Dyslexia
              </button>
              <button
                className={`flex-1 px-3 py-2 text-sm font-medium rounded-md transition-colors ignore-accessibility ${
                  (typeof window !== "undefined" && localStorage.getItem("fontFamily")) === "AtkinsonHyperlegible"
                    ? "bg-white text-gray-900 shadow-sm"
                    : "text-gray-700 hover:text-gray-900"
                }`}
                onClick={() => applyFontFamily("AtkinsonHyperlegible")}
              >
                Low-vision
              </button>
            </div>
          </div>
        </div>

        {/* Contrast Card */}
        <div className="m-3 bg-white rounded-lg shadow-md">
          <div className="pr-3 pl-3 pt-3 pb-1 text-gray-900 ml-0.5 text-lg font-medium ignore-accessibility">
            <div className="flex justify-center items-center">
              <img src={contrast_icon} alt="" className="w-10 h-10" />
            </div>
            <h2>Contrast</h2>
          </div>
          <div className="pr-3 pl-3 pb-3">
            <div className="flex bg-gray-100 rounded-lg p-1">
              <button
                className={`flex-1 px-3 py-2 text-sm font-medium rounded-md transition-colors ignore-accessibility ${
                  (typeof window !== "undefined" && localStorage.getItem("contrastMode")) === "normal"
                    ? "bg-white text-gray-900 shadow-sm"
                    : "text-gray-700 hover:text-gray-900"
                }`}
                onClick={() => applyContrast("normal")}
              >
                Normal
              </button>
              <button
                className={`flex-1 px-3 py-2 text-sm font-medium rounded-md transition-colors ignore-accessibility ${
                  (typeof window !== "undefined" && localStorage.getItem("contrastMode")) === "high-contrast"
                    ? "bg-white text-gray-900 shadow-sm"
                    : "text-gray-700 hover:text-gray-900"
                }`}
                onClick={() => applyContrast("high-contrast")}
              >
                Dark
              </button>
              <button
                className={`flex-1 px-3 py-2 text-sm font-medium rounded-md transition-colors ignore-accessibility ${
                  (typeof window !== "undefined" && localStorage.getItem("contrastMode")) === "inverted"
                    ? "bg-white text-gray-900 shadow-sm"
                    : "text-gray-700 hover:text-gray-900"
                }`}
                onClick={() => applyContrast("inverted")}
              >
                Inverted
              </button>
            </div>
          </div>
        </div>

        {/* Reset Button */}
        <div className="flex justify-center items-center m-3">
          <button
            className="flex justify-center items-center gap-3 ignore-accessibility bg-blue-600 hover:bg-blue-700 text-white w-full px-4 py-3 rounded-lg font-medium transition-colors"
            onClick={resetSettings}
          >
            <img src={reset_icon} alt="" className="w-5 h-5" />
            Reset settings
          </button>
        </div>
      </div>
    </>
  );
}