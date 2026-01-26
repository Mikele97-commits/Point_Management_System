async function callBackend() {
  const res = await fetch("http://localhost:8080/hello");
  const data = await res.json(); 

  document.getElementById("out").innerText =
    data.message + " (" + data.status + ")";
  }

async function sendData(){
  const v1 = document.getElementById("firstName").value;
  const v2 = document.getElementById("lastName").value;
  const v3 = document.getElementById("email").value;
  const allData={
    firstName: v1,
    lastName: v2,
    email: v3
  }
  const jsonData=JSON.stringify(allData)
  
  fetch("http://localhost:8080/add",{
    method: "POST",
    body:jsonData
  });

  console.log("Sent")
}