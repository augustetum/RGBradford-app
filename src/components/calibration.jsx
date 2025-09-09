import React, { useState } from "react";

function Calibration({setWellCenters, wellCenters, setUploadStage, handleFinalSubmit}) {
  const [inputConcentrations, setInputConcentrations] = useState({}); 
  const calibrationWells = wellCenters.filter((c) => c.type === 'STANDARD');
  const maxRow = Math.max(...calibrationWells.map((c) => (c.indexRow)));
  const maxColumn = Math.max(...calibrationWells.map((c) => (c.indexColumn)));
  const minRow = Math.min(...calibrationWells.map((c) => (c.indexRow)));
  const minColumn = Math.min(...calibrationWells.map((c) => (c.indexColumn)));
  
  const alphabet = Object.fromEntries(
    Array.from({ length: 26 }, (_, i) => [i, String.fromCharCode(65 + i)])
  );
  const range = (start, end) => [...Array(end - start + 1)].map((_, i) => start + i);

  function submit(){
    const concentrationValues = Object.values(inputConcentrations)
    const uniqueConcentrations = concentrationValues.filter((item, index) => concentrationValues.indexOf(item) === index)    
    const updatedWellCenters = wellCenters.map(obj => {
      const key = String(obj.indexRow) + '-' + String(obj.indexColumn)
      if (obj.type === 'STANDARD' && inputConcentrations[key]) {
        return ({...obj, standardConcentration: parseFloat(inputConcentrations[key]),  replicateGroup: uniqueConcentrations.indexOf(inputConcentrations[key])})
      } 
      else {return ({...obj})}
    })
    setWellCenters(updatedWellCenters)
    console.log(updatedWellCenters.filter(item => item.type === 'STANDARD'))
    handleFinalSubmit(updatedWellCenters)
  } 
  return(
    <div className="w-[min(90vw, 50rem)]">
      <div className={`grid gap-4`}     
      style={{
      gridTemplateColumns: `4rem repeat(${maxColumn - minColumn + 1}, minmax(0,1fr))`,
      gridTemplateRows: `repeat(${maxRow - minRow + 2}, minmax(0,1fr))`,
    }}>
        {calibrationWells.map((well,i) => {
        const key = `${well.indexRow}-${well.indexColumn}`
        return (<input key={i} className={`inpt bg-green-500 !w-full`}        
          style={{
            gridColumn: `${well.indexColumn - minColumn + 2} / ${well.indexColumn - minColumn + 3}`,
            gridRow: `${well.indexRow - minRow + 2} / ${well.indexRow - minRow + 3}`,
          }}
          value={inputConcentrations[key] || ""}
          onChange={(e) =>
            setInputConcentrations((prev) => ({ ...prev, [key]: e.target.value }))
          } 
          />
       )})}

        <div className="bg-igem-gray rounded-xl border-2 border-black" 
        style={{
            gridColumn: `2 / ${maxColumn - minColumn + 3}`,
            gridRow: `1 / 2`,            
        }}></div>

        {range(minColumn,maxColumn).map((val) => (
          <p 
          key={val}
          className="text-2xl font-bold pt-1"
          style={{
            gridColumn: `${val - minColumn + 2} / ${val - minColumn + 3}`,
            gridRow: `1 / 2`,            
          }}>
            {val+1}
          </p>
        ))}
        <div className="bg-igem-gray rounded-xl border-2 border-black" 
        style={{
            gridRow: `2 / ${maxRow - minRow + 3}`,
            gridColumn: `1 / 2`,            
        }}></div>

        {range(minRow,maxRow).map((val) => (
          <p 
          key={val}
          className="text-2xl font-bold pt-1"
          style={{
            gridRow: `${val - minRow + 2} / ${val - minRow + 3}`,
            gridColumn: `1 / 2`,            
          }}>
            {alphabet[val]}
          </p>
        ))}
      </div>
      <button onClick={() => setUploadStage('wellSelection')} className={`mr-4 mt-4 font-bold !bg-red-500 btn`}>
        Back
      </button>
      <button onClick={submit} className={`mt-4 font-bold !bg-green-500 btn`}>
        Submit
      </button>

      </div>
  )
}

export default Calibration
