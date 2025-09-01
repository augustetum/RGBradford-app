import React, { useState, useRef } from "react";
import plusIcon from "../assets/plus.svg"
import { AnimatePresence, motion } from 'framer-motion';

function Upload() {
  const [image, setImage] = useState(null);
  const [coords, setCoords] = useState({x: 0, y:0});
  const [coordsOrigin, setCoordsOrigin] = useState(null);
  const [coordsEnd, setCoordsEnd] = useState(null);
  const imgRef = useRef(null);
  const fileInputRef = useRef(null);

  const handleInputContainerClick = () => {
    fileInputRef.current.click();
  };

  const handleUpload = (e) => {
    const file = e.target.files[0];
    if (file && file.type.startsWith("image/")) {
      setImage(URL.createObjectURL(file));
    }
  };

  const handleClick = (e) => {
    const img = imgRef.current;
    if (!img) return;
    const rect = img.getBoundingClientRect();

    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    const scaleX = img.naturalWidth / rect.width;
    const scaleY = img.naturalHeight / rect.height;

    const pixelX = Math.round(x * scaleX);
    const pixelY = Math.round(y * scaleY);

    setCoords({ x: pixelX, y: pixelY });
  };

  return (
    <>
          <AnimatePresence mode="wait">
      <motion.div
       key={image}
       initial={{ opacity: 0, y: 20 }}
       animate={{ opacity: 1, y: 0 }}
       exit={{ opacity: 0, y: -20 }}
       transition={{ duration: 0.2 }}
      >
        {!image &&(<button onClick={handleInputContainerClick} className='cursor-pointer w-[min(80vw,50rem)] h-[30vh] bg-igem-gray 
          rounded-xl flex justify-center items-center'>
        <div>
          <img src={plusIcon} className='mx-auto w-10 ' />
          <input
          type="file"
          accept="image/*"
          onChange={handleUpload}
          ref={fileInputRef}
          className="hidden text-center"
          />
          <p>upload plate</p>
        </div>
      </button>)}

      {image && (<div className="my-6">
      
        <div className=" w-[min(80vw,50rem)] p-4 bg-igem-gray 
          rounded-xl flex flex-col">
          <img
            src={image}
            alt="preview"
            ref={imgRef}
            onClick={handleClick}
            className="mx-auto w-auto max-h-[50vh] rounded-lg cursor-crosshair"
          />
          
          <p className="mt-4 !text-igem-black  text-lg font-mono">
            Clicked at: ({coords.x}, {coords.y})
          </p>
        

        </div>
        <div className="flex gap-4 justify-center mt-6">
        <button onClick={() => setImage(null)} className="btn">
          Clear Image
        </button>
        <button onClick={() => setCoordsOrigin(coords)} className="btn">
          Set Origin Coordinates
        </button>
        <button onClick={() => setCoordsEnd(coords)} className="btn">
          Set End Coordinates
        </button>
        <button className="btn !bg-white font-bold">
          Submit
        </button>
        </div>
        <form className="mt-6 flex gap-4 text-xl justify-center" action="">
          <span ><p className="!text-white">Columns</p><input className="inpt" type="number" name="columns" id="columns" defaultValue="12" /></span>
          <span><p className="!text-white">Rows</p><input className="inpt" type="number" name="rows" id="rows" defaultValue="8" /></span>
          <span><p className="!text-white">xOrigin</p><input className="inpt" readOnly={true} type="number" name="xOrigin" id="xOrigin" value={coordsOrigin ? coordsOrigin.x : ""} /></span>
          <span><p className="!text-white">yOrigin</p><input className="inpt" readOnly={true} type="number" name="yOrigin" id="yOrigin" value={coordsOrigin ? coordsOrigin.y : ""} /></span>
          <span><p className="!text-white">xEnd</p><input className="inpt" readOnly={true} type="number" name="xEnd" id="xEnd" value={coordsEnd ? coordsEnd.x : ""} /></span>
          <span><p className="!text-white">yEnd</p><input className="inpt" readOnly={true} type="number" name="yEnd" id="yEnd" value={coordsEnd ? coordsEnd.y : ""} /></span>
        </form>
    </div>)}
    </motion.div>
    </AnimatePresence>
    </>
  );
}

export default Upload