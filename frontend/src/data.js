const data = {
    name : "Jonas",
    projects: [
        {
            id : 0,
            projectTitle : "School project #1",
            creationDate : "2025-05-15",
            points: [
                {
                "concentration": 1,
                "blueToGreenRatio": 0
                },
                {
                "concentration": 2,
                "blueToGreenRatio": 1
                }
            ],
            regression: {
                "slope": 1,
                "intercept": 1,
                "rsquared": 1
            }
        },
        {
            id : 1,
            projectTitle : "School project #2",
            creationDate : "2025-08-21",
            contents: "the bacteria survived this time",
            curve: {
                lineY: [0.1,0.4],
                lineX: [0.2, 0.6],
                lineR2: 0.95,
                pointsY: [0.1,0.2,0.24,0.35],
                pointsX: [0.21,0.3, 0.32, 0.56],
            },
        }, 
    ]
}
export default data