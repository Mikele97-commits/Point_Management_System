

async function showUsers() {
  let number=0
  const res = await fetch("http://localhost:8080/show");
  const data = await res.json();
  console.log(data)
   const tbody = document.getElementById("usersTable").querySelector("tbody");
  tbody.innerHTML = "";

  data.forEach(user => {
    const row = document.createElement("tr");

    const IDCell = document.createElement("td");
    IDCell.textContent = user.id;
    row.appendChild(IDCell);

    const firstNameCell = document.createElement("td");
    firstNameCell.textContent = user.firstName;
    row.appendChild(firstNameCell);

    const lastNameCell = document.createElement("td");
    lastNameCell.textContent = user.lastName;
    row.appendChild(lastNameCell);

    const emailCell = document.createElement("td");
    emailCell.textContent = user.email;
    row.appendChild(emailCell);

    const activeCell = document.createElement("td");
    if(user.active){
    activeCell.textContent = "Yes";}
    else{
        activeCell.textContent = "No";}
    row.appendChild(activeCell);

    const pointsCell = document.createElement("td");
    pointsCell.textContent = user.points;
    row.appendChild(pointsCell);

    number++;
    tbody.appendChild(row);})
    const input = document.getElementById("counter");
    input.max=number;
    
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
  
  const answer = await fetch("http://localhost:8080/add",{
    method: "POST",
    body:jsonData
  });
  if(!answer.isok){
    const text = await answer.text();
    console.log(text)
    document.getElementById("emailError").textContent = text;
    return;
  }
  document.getElementById("emailError").textContent = "";
}

async function testData() {
  const user1={
    firstName:"Michał",
    lastName:"Minich",
    email:"michalminich@gmail.com"
  }
    const jsonUser1=JSON.stringify(user1)
    
     const user2={
    firstName:"Karolina",
    lastName:"Dubanowska",
    email:"karodub@gmail.com"
  }
    const jsonUser2=JSON.stringify(user2)

    const user3={
    firstName:"Marcella",
    lastName:"Ciempa",
    email:"marciemp@gmail.com"
  }
    const jsonUser3=JSON.stringify(user3)
    

    fetch("http://localhost:8080/add",{
    method: "POST",
    body:jsonUser1
  });

  fetch("http://localhost:8080/add",{
    method: "POST",
    body:jsonUser2
  });

  fetch("http://localhost:8080/add",{
    method: "POST",
    body:jsonUser3
  });

}


async function activate() {
  let ID = document.getElementById("counter").value;
  
  const answer = await fetch("http://localhost:8080/activate",{
    method: "POST",
    body:ID
  });
  const text = await answer.text();
  console.log(text);
  showUsers();
}

async function changePoints(positive) {
    let ID = document.getElementById("counter").value;
    let points=document.getElementById("points").value;

     if(!positive)
      points=-points;

    const answer = await fetch("http://localhost:8080/points",{
    method: "POST",
    body:ID + "," + points
    });

    const text = await answer.text();
    document.getElementById("pointsError").textContent = text;

    showUsers();
}

async function writeDB(){
  const answer = await fetch("http://localhost:8080/write");
  const text = await answer.text();
  console.log(text);
}

async function readDB(){
  const answer = await fetch("http://localhost:8080/read");
  const text = await answer.text();
  console.log(text);
}