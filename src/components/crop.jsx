import React from 'react';

function Crop({
  setImage,
  toggleSelectMode,
  setSizeMode,
  selectMode,
  crop,
  selection,
  reset,
  setRefFromUrl,
  displayedImage,
  setUploadStage,
  setSelectMode,
  setOriginalImage,
  setOldSelection,
}) {
  return (
    <div className="w-[min(90vw,50rem)] flex gap-4 justify-center mt-6 flex-wrap">
      <button onClick={() => {setImage(null); setUploadStage('upload')}} className="btn">
        Clear Image
      </button>
      <button
        onClick={() => {
          toggleSelectMode();
          setSizeMode(false);
        }}
        className={`btn ${
          selectMode ? "!bg-red-600 !text-white" : ""
        }`}
      >
        {selectMode ? "Exit Crop Mode" : "Enter Crop Mode"}
      </button>
      <button
        onClick={crop}
        disabled={!selection || selection.w === 0 || selection.h === 0}
        className="btn"
      >
        Crop
      </button>
      <button
        onClick={reset}
        className="btn"
      >
        Reset To Original
      </button>
      <button
        onClick={() => {
          setRefFromUrl(displayedImage);
          setUploadStage('parameters');
          setSelectMode(false);
          setImage(displayedImage);
          setOriginalImage(displayedImage);
          setOldSelection(null);
        }}
        className="btn !bg-green-500 font-bold"
      >
        Submit
      </button>
    </div>
  );
};

export default Crop;